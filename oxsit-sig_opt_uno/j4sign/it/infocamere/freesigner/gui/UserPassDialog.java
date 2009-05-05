/**
 *	Freesigner - a j4sign-based open, multi-platform digital signature client
 *	Copyright (c) 2005 Francesco Cendron - Infocamere
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either version 2
 *	of the License, or (at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package it.infocamere.freesigner.gui;



import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

import javax.swing.*;

/**
 * PIN GUI
 * 
 * @author Francesco Cendron
 */

class UserPassDialog extends JDialog implements ActionListener {

    private JPasswordField password = new JPasswordField("");

    private JTextField user = new JTextField("");

    private boolean okPressed;

    private JButton okButton;

    private JButton cancelButton;

    private String credentials = null;

    public String getCredentials() {
        return credentials;
    }

    public UserPassDialog() {
        this.setModal(true);
        this.setTitle("Utente e password Proxy");
        this.setSize(300, 120);
        GridBagConstraints gbc = new GridBagConstraints();
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((d.width - this.getWidth()) / 2, (d.height - 2*this
                .getHeight()) / 2);

        JPanel p1 = new JPanel(new GridLayout(2, 2, 3, 3));
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        p1.add(new JLabel("Utente:"), gbc);
        user.setSize(8, 1);
        user.addActionListener(this);
        p1.add(user);
        

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        p1.add(new JLabel("Password:"), gbc);
        password.setSize(8, 1);
        password.addActionListener(this);
        p1.add(password);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.bottom = 1;
        gbc.insets.right = 5;

        contentPane.add(p1, gbc);

        Panel p2 = new Panel();
        okButton = addButton(p2, "OK");
        cancelButton = addButton(p2, "Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets.bottom = 1;
        gbc.insets.right = 5;

        contentPane.add(p2, gbc);
    }

    public void setCredentials(String newCredentials) {
        this.credentials = newCredentials;
    }

    private JButton addButton(Container c, String name) {
        JButton button = new JButton(name);
        button.addActionListener(this);
        c.add(button);
        return button;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if ((source == okButton) || (source == password)) {
            setCredentials(user.getText().trim() + ":"
                    + new String(password.getPassword()).trim());

            okPressed = true;

            if (credentials.equals(":") || 
                credentials.startsWith(":") || 
                credentials.endsWith(":")) {
                
                JOptionPane.showMessageDialog(this,
                        "Occorre inserire utente e password del proxy per proseguire!",
                        "Errore di inserimento", JOptionPane.ERROR_MESSAGE);
                setCredentials(null);
            }else
                clearAndHide();
        } else if (source == cancelButton) {
            setCredentials(null);
            clearAndHide();
        }

    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        user.setText(null);
        password.setText(null);
        setVisible(false);
    }
    //    
    // public void actionPerformed(ActionEvent evt) {
    // Object source = evt.getSource();
    // if ((source == okButton) || (source == password)) {
    // okPressed = true;
    // setVisible(false);
    // } else if (source == cancelButton) {
    // setVisible(false);
    //
    // }
    //
    // }

    // public boolean showDialog(ProxyCredentials transfer) {
    //
    // password.setText(transfer.getPassword());
    // user.setText(transfer.getUser());
    //
    // okPressed = false;
    // show();
    // if (okPressed) {
    // transfer.setUser(new String(user.getText()));
    // transfer.setPassword(new String(password.getPassword()));
    // }
    //
    // return okPressed;
    // }
    // class ProxyCredentials { // private String user;
    //
    // private String password;
    //
    // public ProxyCredentials(String u, String p) {
    // user = u;
    // password = p;
    // }
    //
    // public String getPassword() {
    // return password;
    // }
    //
    // public String getUser() {
    // return user;
    // }
    //
    // public void setPassword(String password) {
    // this.password = password;
    // }
    //
    // public void setUser(String user) {
    // this.user = user;
    // }

}