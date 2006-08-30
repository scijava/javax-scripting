function init(applet) {
   var name = window.document.getElementById("name");
   var b = new javax.swing.JButton(name.value);
   applet.add(b, "Center");
   b.addActionListener(function() {
       var name = window.document.getElementById("name");
       b.setText(name.value);
    });
}