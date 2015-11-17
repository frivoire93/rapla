package org.rapla.plugin.exchangeconnector.client;

import java.beans.PropertyChangeListener;

import javax.inject.Inject;

import org.rapla.RaplaResources;
import org.rapla.client.extensionpoints.PublishExtensionFactory;
import org.rapla.client.swing.PublishExtension;
import org.rapla.facade.CalendarSelectionModel;
import org.rapla.facade.ClientFacade;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.framework.logger.Logger;
import org.rapla.inject.Extension;
import org.rapla.plugin.exchangeconnector.ExchangeConnectorPlugin;
import org.rapla.plugin.exchangeconnector.ExchangeConnectorRemote;
import org.rapla.plugin.exchangeconnector.ExchangeConnectorResources;

@Extension(id=ExchangeConnectorPlugin.PLUGIN_ID, provides=PublishExtensionFactory.class)
public class ExchangeExtensionFactory extends RaplaComponent implements PublishExtensionFactory
{
	private final ExchangeConnectorRemote remote;
    private final ExchangeConnectorResources exchangeConnectorResources;
	@Inject
	public ExchangeExtensionFactory(ClientFacade facade, RaplaResources i18n, RaplaLocale raplaLocale, Logger logger, ExchangeConnectorRemote remote, ExchangeConnectorResources exchangeConnectorResources)
	{
		super(facade, i18n, raplaLocale, logger);
		this.remote = remote;
        this.exchangeConnectorResources = exchangeConnectorResources;
	}

	public PublishExtension creatExtension(CalendarSelectionModel model,
			PropertyChangeListener revalidateCallback) throws RaplaException 
	{
		return new ExchangePublishExtension(getClientFacade(), getI18n(), getRaplaLocale(), getLogger(), model,remote, exchangeConnectorResources);
	}

	
}
