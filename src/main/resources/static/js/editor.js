let currentNoteId = NOTE_ID;

function setStatus(text, state) {
    const dot   = document.getElementById('statusDot');
    const label = document.getElementById('statusText');
    if (!dot || !label) return;
    dot.className = 'status-dot' + (state ? ' ' + state : '');
    label.textContent = text;
}

// ── Initialize Quill editor ───────────────────────────────────
const quill = new Quill('#editor', {
    theme: 'snow',
    placeholder: 'Start typing your note here...',
    modules: {
        toolbar: {
            container: [
                [{ header: [1, 2, 3, false] }],
                ['bold', 'italic', 'underline', 'strike'],
                [{ list: 'ordered' }, { list: 'bullet' }],
                [{ align: [] }],
                ['image', 'link'],
                ['clean']
            ],
            handlers: { image: imageHandler }
        }
    }
});

// ── Load existing note if editing ─────────────────────────────
if (currentNoteId) {
    fetch('/api/notes/' + currentNoteId)
        .then(r => r.json())
        .then(note => {
            document.getElementById('noteTitle').value = note.title || '';
            if (note.content) {
                quill.clipboard.dangerouslyPasteHTML(note.content);
            }
            setStatus('All changes saved', 'saved');
        })
        .catch(() => showToast('Error loading note'));
} else {
    setStatus('New note', '');
}

// ── Image upload handler ──────────────────────────────────────
// Converts image to base64 and embeds it directly in the note
function imageHandler() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.click();

    input.onchange = () => {
        const file = input.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = (e) => {
            const range = quill.getSelection(true);
            quill.insertEmbed(range.index, 'image', e.target.result);
            quill.setSelection(range.index + 1);
        };
        reader.readAsDataURL(file);
    };
}

// ── Save note to database ─────────────────────────────────────
async function saveNote() {
    const title   = document.getElementById('noteTitle').value.trim() || 'Untitled';
    const content = quill.root.innerHTML;

    const url    = currentNoteId ? `/api/notes/${currentNoteId}` : '/api/notes';
    const method = currentNoteId ? 'PUT' : 'POST';

    setStatus('Saving…', 'saving');

    try {
        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, content })
        });

        if (response.ok) {
            const saved = await response.json();
            if (!currentNoteId) {
                currentNoteId = saved.id;
                window.history.replaceState({}, '', `/edit/${saved.id}`);
            }
            setStatus('All changes saved', 'saved');
            showToast('Note saved!');
        } else {
            setStatus('Save failed', 'error');
            showToast('Error saving note');
        }
    } catch (err) {
        setStatus('Save failed', 'error');
        showToast('Error saving note');
    }
}

// ── Download as PDF ───────────────────────────────────────────
async function downloadPDF() {
    const { jsPDF } = window.jspdf;
    const element = document.getElementById('note-content');

    showToast('Generating PDF...');

    const canvas = await html2canvas(element, {
        scale: 2,
        useCORS: true,
        backgroundColor: '#ffffff'
    });

    const imgData      = canvas.toDataURL('image/png');
    const pdf          = new jsPDF('p', 'mm', 'a4');
    const pageWidth    = 210;
    const pageHeight   = 297;
    const margin       = 10;
    const contentWidth = pageWidth - margin * 2;
    const imgHeight    = (canvas.height * contentWidth) / canvas.width;

    let heightLeft = imgHeight;
    let yPosition  = margin;

    pdf.addImage(imgData, 'PNG', margin, yPosition, contentWidth, imgHeight);
    heightLeft -= (pageHeight - margin * 2);

    while (heightLeft > 0) {
        pdf.addPage();
        yPosition = margin - (imgHeight - heightLeft);
        pdf.addImage(imgData, 'PNG', margin, yPosition, contentWidth, imgHeight);
        heightLeft -= (pageHeight - margin * 2);
    }

    const title = document.getElementById('noteTitle').value.trim() || 'my-note';
    pdf.save(`${title}.pdf`);
}

// ── Toast notification ────────────────────────────────────────
function showToast(message) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 2500);
}

// ── Auto-save every 30 seconds ────────────────────────────────
setInterval(() => {
    if (quill.getText().trim().length > 1) {
        saveNote();
    }
}, 30000);
