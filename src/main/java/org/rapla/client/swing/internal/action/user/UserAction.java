/*--------------------------------------------------------------------------*
 | Copyright (C) 2014 Christopher Kohlhaas, Bettina Lademann                |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.client.swing.internal.action.user;

import org.rapla.client.ClientService;
import org.rapla.entities.User;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.client.swing.EditController;
import org.rapla.client.PopupContext;
import org.rapla.client.swing.RaplaAction;

public class UserAction extends RaplaAction {
    Object object;
    public final int NEW = 1;
    public final int SWITCH_TO_USER = 3;
    int type = NEW;
    private final PopupContext popupContext;
    private final ClientService service;
    private final EditController editController;

    public UserAction(RaplaContext sm,PopupContext popupContext, ClientService service, EditController editController) {
        super(sm);
        this.popupContext = popupContext;
        this.service = service;
        this.editController = editController;
    }

    public UserAction setNew() {
        type = NEW;
        putValue(NAME, getString("user"));
        putValue(SMALL_ICON, getIcon("icon.new"));
        update();
        return this;
    }

    public UserAction setSwitchToUser() {
        type = SWITCH_TO_USER;
        if (service.canSwitchBack()) {
            putValue(NAME, getString("switch_back"));
        } else {
            putValue(NAME, getString("switch_to"));
        }
        return this;
    }

    public void changeObject(Object object) {
        this.object = object;
        update();
    }

    private void update() {
        User user = null;
        try {
            user = getUser();
            if (type == NEW) {
                setEnabled(isAdmin());
            } else if (type == SWITCH_TO_USER) {
                setEnabled(service.canSwitchBack() || (object != null && isAdmin() && !user.equals(object)));
            }
        } catch (RaplaException ex) {
            setEnabled(false);
            return;
        }

    }

    public void actionPerformed() {
        try {
            if (type == SWITCH_TO_USER) {
            	if (service.canSwitchBack()) {
                    service.switchTo(null);
                    //putValue(NAME, getString("switch_to"));
                } else if ( object != null ){
                    service.switchTo((User) object);
                   // putValue(NAME, getString("switch_back"));
                }
            } else if (type == NEW) {
                User newUser = getModification().newUser();
                // create new user dialog and show password dialog if user is created successfully
                final String title = getString("user");
                editController.edit(newUser, title,popupContext,new EditController.EditCallback<User>()
                    {
                            @Override public void onFailure(Throwable e)
                            {
                                showException( e, popupContext);
                            }

                            @Override public void onSuccess(User editObject)
                            {
                                object = editObject;
                                if (getUserModule().canChangePassword())
                                {
                                    try
                                    {
                                        changePassword(editObject, false);
                                    }
                                    catch (RaplaException e)
                                    {
                                        onFailure(e);
                                    }
                                }
                            }

                            @Override public void onAbort()
                            {
                            }
                        }
                );

            }
        } catch (RaplaException ex) {
            showException(ex, popupContext);
        }
    }

    public void changePassword(User user,boolean showOld) throws RaplaException{
        new PasswordChangeAction(getContext(),popupContext).changePassword( user, showOld);
    }

}