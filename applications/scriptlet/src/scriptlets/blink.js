var color = 1;
var font;

function init(applet) {
    timer = setTimeout(function() { 
        applet.repaint(); 
    }, 700);
    var Font = java.awt.Font;
    font = new Font(Font.DIALOG, Font.BOLD, 18);
}

function paint(applet, g) {
    g.clearRect(0,0,100,50); 
    g.setFont(font);
    if (color) {
        g.setColor(java.awt.Color.RED);
        color = 0;
    } else {
        g.setColor(java.awt.Color.BLUE);
        color = 1;
    }
    g.drawString('hello world', 0, 20); 
}

function destroy(applet) {
    clearTimeout(timer);
}