jQuery(document).ready(function() {

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