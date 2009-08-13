jQuery(document).ready(function() {

$("a.star").click(function() {
  star = $(this);
  id = star.next("a").attr("id");
  starred = star.hasClass("starred")
  if (starred) {
    star.html("&#9734;");
  } else {
    star.html("&#9733;");
  }
  toggleStar(id, !starred)
  star.toggleClass("starred");
});

toggleStar = function(id, starred) {
  $.post("/post/star", {id: id, s: starred});
}

$("a.star").mouseover(function() {
  $(this).addClass("cursored");
});

$("a.delete").mouseover(function() {
  $(this).addClass("cursored");
});

$("a.delete").click(function() {
  title = $(this).prev("a").text();
  if (confirm('Are you sure you want to delete "' + title + '"?'))
  {
    id = $(this).prev("a").attr("id");
    parent = $(this).parents("div.post")
    $.post("/post/delete", {id: id},
      function(data) {
        parent.slideToggle("slow");
      });
  }
});

});