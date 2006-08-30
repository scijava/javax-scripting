function paint(applet, g) {
    g.clearRect(0, 0,200, 200);
    g.setColor(java.awt.Color.RED);
    g.fillRect (10, 10, 50, 50);
    g.setColor(java.awt.Color.BLUE);
    g.fillRect (30, 30, 50, 50);
    g.setColor(java.awt.Color.GREEN);
    g.fill3DRect(50, 50, 100, 100, true);
    g.setColor(java.awt.Color.ORANGE);
    g.fillOval(60, 60, 120, 120);
}