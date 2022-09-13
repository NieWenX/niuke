
function like(btn, entityType, entityId, entityUserId) {
    $.post(
        "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId},
        function (data) {
            console.info(data);
            // data = $.parseJSON(data);   //
            console.info(data);
            if (data.code === "0000") {
                $(btn).children("i").text(data.data.likeCount);
                $(btn).children("b").text(data.data.likeStatus === 1 ? '已赞' : '赞');
            } else {
                alert(data.msg);
            }
        }
    );
}