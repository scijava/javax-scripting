var timer;

function init(applet) {
   timer = setTimeout(function () {
           applet.repaint();
        }, 50);
}

function paint(applet, g) {
    // clear the whole area once in a while
    if (Math.random() > 0.99) {
        g.clearRect(0,0,500,100);       
    }

    g.setColor(new java.awt.Color(
             Math.random()*1.0,
             Math.random()*1.0,
             Math.random()*1.0));

    g.fillRect(Math.random()*500,
             Math.random()*100,
             Math.random()*100, 
             Math.random()*100);
}

function destroy(applet) {
    clearTimeout(timer);
}    