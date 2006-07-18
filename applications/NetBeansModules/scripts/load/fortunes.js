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
//Fortunes copied from /usr/share/fortune/fortune
//Routine copied from http://www.public.iastate.edu/~jastruss/JS/js12.html
importPackage(Packages.org.chuk.lee.scriptengine.api);

function fortunes() {
	quotes = new Array(10);
	quotes[0]="186,282 miles per second:\nIt isn't just a good idea, it's the law!";
	quotes[1]="A classic is something that everybody wants to have read\nand nobody wants to read.\n\t\t-- Mark Twain";
	quotes[2]="A fanatic is one who can't change his mind\nand won't change the subject.\n\t\t-- Winston Churchill";
	quotes[3]="A journey of a thousand miles begins with a cash advance.";
	quotes[4]="A language that doesn't have everything is actually\neasier to program in than some that do.\n\t\t-- Dennis M. Ritchie";
	quotes[5]="A lot of people are afraid of heights.\nNot me. I'm afraid of widths.\n\t\t-- Steve Wright";
	quotes[6]="A professor is one who talks in someone else's sleep.";
	quotes[7]="A radioactive cat has eighteen half-lives.";
	quotes[8]="A truly wise man never plays leapfrog with a unicorn.";
	quotes[9]="Measure with a micrometer.\nMark with chalk.\nCut with an axe.";
	quotes[10]="YOW!!  Everybody out of the GENETIC POOL!";
/* calculate a random index */
	index = Math.floor(Math.random() * quotes.length);
	NetBeans.informationDialog(quotes[index], "Fortune");
}

var _cookie = { invoke : function() { fortunes() }};
//Register to listen for an event. Currently NetBeans object exports 2 events:
//1. NetBeans.startup - called when NetBeans starts up
//2. NetBeans.shutdown - called when NetBeans 
//ModuleEventListener is a Java interface in org.chuk.lee.scriptengine.api
//package. Has exactly 1 method with the following sig:
//		public void invoke();
NetBeans.register("NetBeans.startup", ModuleEventListener(_cookie));
