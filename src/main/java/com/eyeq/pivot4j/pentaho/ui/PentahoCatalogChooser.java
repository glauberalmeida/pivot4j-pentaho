package com.eyeq.pivot4j.pentaho.ui;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;

import com.eyeq.pivot4j.pentaho.datasource.PentahoDataSourceManager;
import com.eyeq.pivot4j.primefaces.datasource.ConnectionMetadata;
import com.eyeq.pivot4j.primefaces.state.ViewState;
import com.eyeq.pivot4j.primefaces.state.ViewStateHolder;

@ManagedBean(name = "pentahoCatalogChooser")
@ViewScoped
public class PentahoCatalogChooser {

	@ManagedProperty(value = "#{dataSourceManager}")
	private PentahoDataSourceManager dataSourceManager;

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	private List<UISelectItem> catalogItems;

	private List<UISelectItem> cubeItems;

	private String catalogName;

	private String cubeName;

	private String viewId;

	/**
	 * @return the dataSourceManager
	 */
	public PentahoDataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	/**
	 * @param dataSourceManager
	 *            the dataSourceManager to set
	 */
	public void setDataSourceManager(PentahoDataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}

	/**
	 * @return the viewStateHolder
	 */
	public ViewStateHolder getViewStateHolder() {
		return viewStateHolder;
	}

	/**
	 * @param viewStateHolder
	 *            the viewStateHolder to set
	 */
	public void setViewStateHolder(ViewStateHolder viewStateHolder) {
		this.viewStateHolder = viewStateHolder;
	}

	/**
	 * @return the catalogName
	 */
	public String getCatalogName() {
		return catalogName;
	}

	/**
	 * @param catalogName
	 *            the catalogName to set
	 */
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	/**
	 * @return the cubeName
	 */
	public String getCubeName() {
		return cubeName;
	}

	/**
	 * @param cubeName
	 *            the cubeName to set
	 */
	public void setCubeName(String cubeName) {
		this.cubeName = cubeName;
	}

	/**
	 * @return the viewId
	 */
	public String getViewId() {
		return viewId;
	}

	/**
	 * @param viewId
	 *            the viewId to set
	 */
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public List<UISelectItem> getCatalogs() {
		if (catalogItems == null) {
			List<MondrianCatalog> catalogs = dataSourceManager.getCatalogs();

			this.catalogItems = new ArrayList<UISelectItem>(catalogs.size());

			UISelectItem defaultItem = new UISelectItem();
			defaultItem.setItemLabel("---- Please select a catalog ----");
			defaultItem.setItemValue("");

			catalogItems.add(defaultItem);

			for (MondrianCatalog catalog : catalogs) {
				UISelectItem item = new UISelectItem();

				item.setItemLabel(catalog.getName());
				item.setItemValue(catalog.getDefinition());
				catalogItems.add(item);
			}
		}

		return catalogItems;
	}

	public List<UISelectItem> getCubes() {
		if (cubeItems == null) {
			this.cubeItems = new ArrayList<UISelectItem>();

			UISelectItem defaultItem = new UISelectItem();
			defaultItem.setItemLabel("---- Please select a cube ----");
			defaultItem.setItemValue("");

			cubeItems.add(defaultItem);

			MondrianCatalog catalog = getDataSourceManager().getCatalog(
					catalogName);

			if (catalog != null) {
				MondrianSchema schema = catalog.getSchema();

				List<MondrianCube> cubes = schema.getCubes();

				for (MondrianCube cube : cubes) {
					UISelectItem item = new UISelectItem();
					item.setItemLabel(cube.getName());
					item.setItemValue(cube.getName());

					cubeItems.add(item);

					if (cubeName == null) {
						this.cubeName = cube.getName();
					}
				}
			}
		}

		return cubeItems;
	}

	public void onCatalogChanged() {
		this.cubeName = null;
		this.cubeItems = null;
	}

	public boolean isNew() {
		return viewId == null || viewStateHolder.getState(viewId) == null;
	}

	public void checkState() {
		if (viewId != null) {
			ViewState state = viewStateHolder.getState(viewId);

			if (state != null) {
				ConnectionMetadata connectionInfo = state.getConnectionInfo();

				this.catalogName = connectionInfo.getCatalogName();
				this.cubeName = connectionInfo.getCubeName();

				FacesContext facesContext = FacesContext.getCurrentInstance();

				NavigationHandler navigationHandler = facesContext
						.getApplication().getNavigationHandler();
				navigationHandler.handleNavigation(facesContext, null,
						"view?faces-redirect=true&ts=" + viewId);
			}
		}
	}

	public String proceed() {
		FacesContext context = FacesContext.getCurrentInstance();
		Flash flash = context.getExternalContext().getFlash();

		ConnectionMetadata connectionInfo = new ConnectionMetadata(catalogName,
				cubeName);
		flash.put("connectionInfo", connectionInfo);
		flash.put("viewId", viewId);

		return "view?faces-redirect=true&ts=" + viewId;
	}
}