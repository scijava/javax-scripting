/*
Copyright © 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa  Clara, California
95054, U.S.A. All rights reserved.U.S. Government Rights - Commercial software.
Government users are subject to the Sun Microsystems, Inc. standard license agreement
and applicable provisions of the FAR and its supplements.  Use is subject to license
terms.  Sun, Sun Microsystems,  the Sun logo,  Java and  Jini are trademarks or registered
trademarks of Sun Microsystems, Inc. in the U.S. and other countries. This product is
covered and controlled by U.S. Export Control laws and may be subject to the export or import
laws in other countries.  Nuclear, missile, chemical biological weapons or nuclear maritime 
end uses or end users, whether direct or indirect, are strictly prohibited. Export or reexport
to countries subject to U.S. embargo or to entities identified on U.S. Export exclusion lists,
including, but not limited to, the denied persons and specially designated nationals lists is
strictly prohibited.

Copyright © 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,  California 95054,
Etats-Unis. Tous droits réservés.L'utilisation est soumise aux termes de la Licence.Sun, Sun
Microsystems,  le logo Sun,  Java et  Jini sont des  marques de fabrique ou des marques 
déposées de Sun Microsystems, Inc. aux Etats-Unis et dans d'autres pays.Ce produit est soumis
à la législation américaine en matière de contrôle des exportations et peut être soumis à la 
règlementation en vigueur dans d'autres pays dans le domaine des exportations et importations.
Les utilisations, ou utilisateurs finaux, pour des armes nucléaires,des missiles, des armes 
biologiques et chimiques ou du nucléaire maritime, directement ou indirectement, sont 
strictement interdites. Les exportations ou réexportations vers les pays sous embargo 
américain, ou vers des entités figurant sur les listes d'exclusion d'exportation américaines,
y compris, mais de manière non exhaustive, la liste de personnes qui font objet d'un ordre de
ne pas participer, d'une façon directe ou indirecte, aux exportations des produits ou des 
services qui sont régis par la législation américaine en matière de contrôle des exportations
et la liste de ressortissants spécifiquement désignés, sont rigoureusement interdites.
*/
importPackage(Packages.java.lang);
importPackage(Packages.javax.swing);
importPackage(Packages.java.awt.event);
importPackage(Packages.java.awt);

//Mixture of JavaScript and Java
var frame = new JFrame("JavaScript Clock");
var panel = new JPanel();
var button = new JButton("Press me");
panel.add(button);
frame.add(panel, "South")
panel = new JPanel();
var label = new JLabel(new Date());
label.setForeground(Color.BLUE);
panel.add(label);
frame.add(panel, "North");
var action = { actionPerformed : function(event) { frame.setVisible(false); } };
var actionListener = ActionListener(action);
button.addActionListener(actionListener);
var runit = { run : function() { 
	while (true) {
		Thread.sleep(1000);
		label.setText(new Date());
	}
} };
var runnable = Runnable(runit);
var thr = new Thread(runnable);
thr.start();
frame.pack();
frame.setVisible(true);
