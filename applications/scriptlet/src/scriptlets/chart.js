
function init(applet) {
   var b = new javax.swing.JButton("change chart");
   applet.add(b, "South");
   b.addActionListener(function() { applet.repaint(); });
}

function chart(g, x, w, y) {
    g.fillRect(x, 100-y, w, y);
}

function paint(applet, g) {
    g.clearRect(0, 0, 200, 75); 
    var w = 5;
    g.setColor(java.awt.Color.RED);
    for (var x = 0; x < 500; x+=w) {
        chart(g, x, w, 100*Math.random());
    }
}