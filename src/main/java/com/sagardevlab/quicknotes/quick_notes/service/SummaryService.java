package com.sagardevlab.quicknotes.quick_notes.service;

import jakarta.mail.internet.MimeMessage;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class SummaryService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${openai.api.key}")
    private String openAiKey;

    @Value("${app.url}")
    private String appUrl;

    private final RestClient restClient = RestClient.create();

    public void summarizeAndEmail(String noteTitle, String noteHtml, Long noteId,
                                   String userEmail, String userName) {

        String plainText = Jsoup.parse(noteHtml).text();
        String summary   = callOpenAI(noteTitle, plainText);
        sendEmail(noteTitle, summary, noteId, userEmail, userName);
    }

    private String callOpenAI(String title, String text) {
        String prompt = """
                You are a helpful study assistant. Summarize the following note titled "%s" into 5–7 concise bullet points.
                Each bullet point should start with a dash (-). Focus on the key ideas so the reader can revise in under 2 minutes.
                Note content:
                %s
                """.formatted(title, text.length() > 4000 ? text.substring(0, 4000) : text);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openAiKey)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "model", "gpt-3.5-turbo",
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "max_tokens", 512,
                        "temperature", 0.5
                ))
                .retrieve()
                .body(Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private void sendEmail(String title, String summary, Long noteId,
                            String toEmail, String userName) {
        try {
            String bullets = buildBulletHtml(summary);
            String noteLink = appUrl + "/notes";

            String html = """
                    <!DOCTYPE html>
                    <html><head><meta charset="UTF-8">
                    <style>
                      body{margin:0;padding:0;background:#0e0c1e;font-family:'Segoe UI',Arial,sans-serif;}
                      .wrap{max-width:560px;margin:32px auto;background:#130f28;border-radius:18px;overflow:hidden;border:1px solid rgba(167,139,250,.2);}
                      .header{background:linear-gradient(135deg,#7c3aed,#db2777);padding:32px 36px 28px;}
                      .header h1{margin:0;font-size:20px;color:#fff;font-weight:700;}
                      .header p{margin:6px 0 0;font-size:13px;color:rgba(255,255,255,.75);}
                      .body{padding:28px 36px 24px;}
                      .note-title{font-size:17px;font-weight:700;color:#c4b5fd;margin:0 0 18px;}
                      ul{margin:0;padding:0 0 0 18px;}
                      ul li{color:#e2d9f3;font-size:14.5px;line-height:1.7;margin-bottom:6px;}
                      .cta{display:inline-block;margin-top:24px;padding:11px 26px;background:linear-gradient(135deg,#7c3aed,#db2777);color:#fff;text-decoration:none;border-radius:10px;font-size:13.5px;font-weight:600;}
                      .footer{padding:16px 36px 22px;border-top:1px solid rgba(167,139,250,.12);font-size:11.5px;color:rgba(255,255,255,.3);text-align:center;}
                    </style></head>
                    <body>
                    <div class="wrap">
                      <div class="header">
                        <h1>📝 Quick Revision — NoteHelper</h1>
                        <p>Hi %s, here's your 2-minute revision summary</p>
                      </div>
                      <div class="body">
                        <p class="note-title">%s</p>
                        <ul>%s</ul>
                        <a href="%s" class="cta">View Full Note →</a>
                      </div>
                      <div class="footer">made by sagardevlab &nbsp;·&nbsp; NoteHelper</div>
                    </div>
                    </body></html>
                    """.formatted(userName, escHtml(title), bullets, noteLink);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("📝 Revision: " + title);
            helper.setText(html, true);
            mailSender.send(msg);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send summary email", e);
        }
    }

    private String buildBulletHtml(String summary) {
        StringBuilder sb = new StringBuilder();
        for (String line : summary.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("-")) trimmed = trimmed.substring(1).trim();
            if (!trimmed.isEmpty()) sb.append("<li>").append(escHtml(trimmed)).append("</li>");
        }
        return sb.toString();
    }

    private String escHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
