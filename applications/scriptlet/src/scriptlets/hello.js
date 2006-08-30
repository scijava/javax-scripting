var font;

function init(applet) {
    var Font = java.awt.Font;
    font = new Font(Font.DIALOG, Font.BOLD, 18);
}

function paint(applet, g) {
    g.setFont(font);
    g.clearRect(0,0,100,50); 
    g.setColor(java.awt.Color.RED);
    g.drawString('hello world', 0, 20); 
}