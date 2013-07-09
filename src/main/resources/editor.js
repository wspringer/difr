/*
 * Difr
 * Copyright (C) 2013  Wilfred Springer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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