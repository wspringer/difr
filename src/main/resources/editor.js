$().ready(function(){

    var editing = false;

    $(document).on('click', '.line', function(e) {
        e.preventDefault();
        if (!editing) {
            var line = $(e.target).closest(".line");
            var editor = ich.editor({});
            var contents = editor.find(".contents");
            var form = editor.find(".form");
            var cancelBtn = editor.find(".cancel");

            function hide(fn) {
                contents.off('keyup');
                cancelBtn.off('click');
                form.slideUp(200, function() {
                    editor.remove();
                    editing = false;
                    if (fn) fn();
                });
            }

            function cancel() {
                hide();
            }

            function save() {
                hide(function () {
                    console.info("Saved " + contents.val());
                    var comments = contents.val();
                    var commentbox = ich.comments({comments:comments});
                    commentbox.insertAfter(line);
                });
            }

            contents.keyup(function (e) {
               switch (event.which) {
                   case 13: event.preventDefault(); save(); break;
                   case 27: event.preventDefault(); cancel(); break;
               }
            });

            editor.insertAfter(line);
            form.slideDown(200);
            cancelBtn.on('click', function() { console.info("Cancelled");cancel(); });
            contents.focus();
            editing = true;
        }
    });

});