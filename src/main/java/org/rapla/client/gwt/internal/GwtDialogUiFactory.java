package org.rapla.client.gwt.internal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import org.rapla.RaplaResources;
import org.rapla.client.PopupContext;
import org.rapla.client.RaplaWidget;
import org.rapla.client.dialog.DialogInterface;
import org.rapla.client.dialog.DialogInterface.DialogAction;
import org.rapla.client.dialog.DialogUiFactoryInterface;
import org.rapla.client.gwt.GwtPopupContext;
import org.rapla.client.menu.data.Point;
import org.rapla.entities.DependencyException;
import org.rapla.framework.Disposable;
import org.rapla.inject.DefaultImplementation;
import org.rapla.inject.InjectionContext;
import org.rapla.logger.Logger;
import org.rapla.scheduler.Promise;
import org.rapla.scheduler.UnsynchronizedPromise;
import org.rapla.storage.dbrm.RaplaConnectException;
import org.rapla.storage.dbrm.RaplaRestartingException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Singleton
@DefaultImplementation(context = InjectionContext.gwt, of = DialogUiFactoryInterface.class)
public class GwtDialogUiFactory implements DialogUiFactoryInterface
{
    private static int zIndex = 500;

    private static class GwtDialogAction implements DialogAction
    {

        private final Button button;
        private Runnable action;

        public GwtDialogAction(Button button)
        {
            this.button = button;
        }

        @Override
        public void setEnabled(boolean enabled)
        {
            button.setEnabled(enabled);
        }

        @Override
        public void setRunnable(Runnable runnable)
        {
            action = runnable;
        }

        @Override
        public void setIcon(String iconKey)
        {
            // FIXME GWT
        }

        @Override
        public void execute()
        {
            if (action != null)
            {
                action.run();
            }
        }

    }

    private static class GwtDialog extends PopupPanel implements DialogInterface, CloseHandler<PopupPanel>
    {

        private final Button[] buttons;

        private final DialogAction[] actions;

        private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

        private Disposable disposable;

        private int selectedIndex = -1;

        private Runnable abortAction = null;


        public GwtDialog(boolean modal, String[] options)
        {
            super(true, modal);
            buttons = new Button[options.length];
            actions = new DialogAction[options.length];
            for (int i = options.length - 1; i >= 0; i--)
            {
                final int index = i;
                buttons[i] = new Button(options[i]);
                actions[i] = new GwtDialogAction(buttons[i]);
                handlers.add(buttons[i].addClickHandler(new ClickHandler()
                {
                    @Override
                    public void onClick(ClickEvent event)
                    {
                        selectedIndex = index;
                        actions[index].execute();
                        close();
                    }
                }));
            }
        }

        @Override
        public Promise<Integer> start(boolean pack)
        {
            handlers.add(super.addCloseHandler(this));
            show();
            toFront();
            if (selectedIndex != -1)
            {
                buttons[selectedIndex].setFocus(true);
            }
            final UnsynchronizedPromise<Integer> integerUnsynchronizedCompletablePromise = new UnsynchronizedPromise<>();
            return integerUnsynchronizedCompletablePromise;
        }

        @Override
        public void busy(String message) {

        }

        @Override
        public void idle() {

        }

        @Override
        public int getSelectedIndex()
        {
            return selectedIndex;
        }

        @Override
        public void setTitle(String createTitle)
        {
            super.setTitle(createTitle);
        }

        @Override
        public void setIcon(String iconKey)
        {
            // FIXME GWT
        }

        @Override
        public void setSize(int width, int height)
        {
            super.setSize(width + "px", height + "px");
        }

        @Override
        public void close()
        {
            hide();
            removeFromParent();
            removeHandlers();
        }

        private void removeHandlers()
        {
            for (HandlerRegistration handlerRegistration : handlers)
            {
                handlerRegistration.removeHandler();
            }
        }

        @Override
        public boolean isVisible()
        {
            return super.isVisible();
        }

        @Override
        public void setPosition(double left, double top)
        {
            super.setPopupPosition((int) left, (int) top);
        }

        @Override
        public DialogAction getAction(int commandIndex)
        {
            return null;
        }

        @Override
        public void setAbortAction(Runnable abortAction)
        {
            this.abortAction = abortAction;
        }

        @Override
        public void setDefault(int commandIndex)
        {
            selectedIndex = commandIndex;
        }

        public void toFront()
        {
            if (isVisible())
            {
                super.getElement().getStyle().setZIndex(zIndex++);
            }
        }

        @Override
        public void onClose(CloseEvent<PopupPanel> event)
        {
            if (abortAction != null)
            {
                abortAction.run();
            }
            if (disposable != null)
            {
                disposable.dispose();
            }

            selectedIndex = -1;
            removeHandlers();
        }
    }

    private final RaplaResources i18n;
    private final Logger logger;

    @Inject
    public GwtDialogUiFactory(RaplaResources i18n, Logger logger)
    {
        this.i18n = i18n;
        this.logger = logger;
    }

    @Override
    public DialogInterface create(PopupContext popupContext, boolean modal, Object content, String[] options)
    {
        final GwtDialog gwtDialog = new GwtDialog(modal, options);
        final Point point = GwtPopupContext.extractPoint(popupContext);
        if (point != null)
        {
            gwtDialog.setPopupPosition(point.getX(), point.getY());
        }
        return gwtDialog;
    }

    @Override
    public DialogInterface create(PopupContext popupContext, String title, String text, String[] options)
    {
        final DialogInterface di = create(popupContext, false, new Label(text), options);
        di.setTitle(title);
        return di;
    }

    @Override
    public DialogInterface create(PopupContext popupContext, String title, String text)
    {
        final DialogInterface di = create(popupContext, title, text, DialogUiFactoryInterface.Util.getDefaultOptions());
        return di;
    }

    @Override
    public Void showException(Throwable ex, PopupContext popupContext)
    {
        if (ex instanceof RaplaConnectException)
        {
            String message = ex.getMessage();
            Throwable cause = ex.getCause();
            String additionalInfo = "";
            if (cause != null)
            {
                additionalInfo = " " + cause.getClass() + ":" + cause.getMessage();
            }

            logger.warn(message + additionalInfo);
            if (ex instanceof RaplaRestartingException)
            {
                return Promise.VOID;
            }
            try
            {
                final String key = "error";
                final String title = i18n.format("exclamation.format", i18n.getString(key));
                final DialogInterface di = create(popupContext, title, message);
                di.start(true);
            }
            catch (Throwable e)
            {
                logger.error(e.getMessage(), e);
            }
        }
        else
        {
            try
            {
                final String message;
                if (ex instanceof DependencyException)
                {
                    message = getHTML((DependencyException) ex);
                }
                else if (DialogUiFactoryInterface.Util.isWarningOnly(ex))
                {
                    message = ex.getMessage();
                }
                else
                {
                    message = stacktrace(ex);
                }
                final String key = "error";
                final String title = i18n.format("exclamation.format", i18n.getString(key));
                DialogInterface di = create(popupContext, title, message);
                di.start(true);
            }
            catch (Throwable ex2)
            {
                logger.error(ex2.getMessage(), ex2);
            }
        }
        return Promise.VOID;
    }

    private String stacktrace(Throwable ex)
    {
        StringBuilder sb = new StringBuilder();
        final StackTraceElement[] stackTrace = ex.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace)
        {
            sb.append(stackTraceElement.getFileName());
            sb.append(".");
            sb.append(stackTraceElement.getMethodName());
        }
        return sb.toString();
    }

    static private String getHTML(DependencyException ex)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(ex.getMessageText() + ":");
        buf.append("<br><br>");
        Iterator<String> it = ex.getDependencies().iterator();
        int i = 0;
        while (it.hasNext())
        {
            Object obj = it.next();
            buf.append((++i));
            buf.append(") ");

            buf.append(obj);

            buf.append("<br>");
            if (i == 30 && it.hasNext())
            {
                buf.append("... " + (ex.getDependencies().size() - 30) + " more");
                break;
            }
        }
        return buf.toString();
    }

    @Override
    public Void showWarning(String warning, PopupContext popupContext)
    {
    	return Promise.VOID;
    }

    @Override public PopupContext createPopupContext(RaplaWidget widget)
    {
        final Object component = widget.getComponent();
        // todo maybe add component here
        return new GwtPopupContext(null);
    }

    @Override
    public void busy(String message) {

    }

    @Override
    public void idle() {

    }

}
