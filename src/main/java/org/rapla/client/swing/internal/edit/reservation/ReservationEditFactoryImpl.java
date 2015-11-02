package org.rapla.client.swing.internal.edit.reservation;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.rapla.client.ReservationController;
import org.rapla.client.ReservationEdit;
import org.rapla.client.extensionpoints.AppointmentStatusFactory;
import org.rapla.client.internal.ReservationEditFactory;
import org.rapla.client.swing.MenuFactory;
import org.rapla.client.swing.TreeFactory;
import org.rapla.client.swing.extensionpoints.SwingViewFactory;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.AppointmentFormater;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.domain.permission.PermissionController;
import org.rapla.facade.CalendarSelectionModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.inject.DefaultImplementation;
import org.rapla.inject.InjectionContext;

@DefaultImplementation(of= ReservationEditFactory.class,context = InjectionContext.swing)
@Singleton
public class ReservationEditFactoryImpl implements ReservationEditFactory
{
    private final Set<AppointmentStatusFactory> list;
    private final RaplaContext context;
    private final Set<SwingViewFactory> swingViewFactories;
    private final TreeFactory treeFactory;
    private final CalendarSelectionModel calendarSelectionModel;
    private final AppointmentFormater appointmentFormater;
    private final PermissionController permissionController;
    private final ReservationController reservationController;
    private final MenuFactory menuFactory;
    @Inject
    public ReservationEditFactoryImpl(Set<AppointmentStatusFactory> list, RaplaContext context, Set<SwingViewFactory> swingViewFactories,
            TreeFactory treeFactory, CalendarSelectionModel calendarSelectionModel, AppointmentFormater appointmentFormater,
            PermissionController permissionController, ReservationController reservationController, MenuFactory menuFactory)
    {
        this.list = list;
        this.context = context;
        this.swingViewFactories = swingViewFactories;
        this.treeFactory = treeFactory;
        this.calendarSelectionModel = calendarSelectionModel;
        this.appointmentFormater = appointmentFormater;
        this.permissionController = permissionController;
        this.reservationController = reservationController;
        this.menuFactory = menuFactory;
    }
    
    public ReservationEdit create(Reservation reservation, AppointmentBlock appointmentBlock) throws RaplaException
    {
        ReservationEditImpl edit = new ReservationEditImpl(context, list, swingViewFactories, treeFactory, calendarSelectionModel, appointmentFormater, permissionController, reservationController, menuFactory);
        edit.editReservation(reservation, appointmentBlock);
        return edit;
    }

}