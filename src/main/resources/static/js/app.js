var toolbarOptions = [
    ['bold', 'italic'],
    [{'header': [1, 2, 3, 4, 5, 6, false]}],
    ['link', 'image'],
    ['clean']
];

var quill = new Quill('#editor', {
    modules: {
        toolbar: {
            container: toolbarOptions,
            handlers: {
                image: function () {
                    var range = this.quill.getSelection();
                    var value = prompt('Provide the image url here.');

                    if (value) {
                        this.quill.insertEmbed(range.index, 'image', value, Quill.sources.USER);
                    }
                }
            }
        }
    },
    theme: 'snow'
});

var content = document.getElementById('content');
var form = document.getElementsByTagName('form')[0];
form.onsubmit = function () {
    content.value = quill.root.innerHTML;
    return true;
};

window.onload = function () {
    quill.root.innerHTML = content.value;
}
