var URL = {
    index: "index"
};

$(function () {
    $.ajax({
        url: URL.index,
        dataType: "json",
        type: "GET",
        success: function (data) {
            if (data && data.code === 2000) {
                var tableContent = data.data;
                $.each(tableContent, function (key, value) {
                    var pair = "<tr><td>" + key + "</td><td>" + value + "</td></tr>";
                    $("#indexTable").append(pair)
                })
            }
        },
        error: function (data) {
            console.log(data);
        }
    })
});