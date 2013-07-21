package org.gots.garden.provider.nuxeo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.DocumentListener;

import org.gots.garden.GardenInterface;
import org.gots.garden.provider.local.LocalGardenProvider;
import org.gots.nuxeo.NuxeoManager;
import org.nuxeo.android.config.NuxeoServerConfig;
import org.nuxeo.android.context.NuxeoContext;
import org.nuxeo.android.context.NuxeoContextFactory;
import org.nuxeo.android.documentprovider.DocumentsListChangeListener;
import org.nuxeo.android.documentprovider.LazyUpdatableDocumentsList;
import org.nuxeo.android.repository.DocumentManager;
import org.nuxeo.ecm.automation.client.android.AndroidAutomationClient;
import org.nuxeo.ecm.automation.client.android.CachedSession;
import org.nuxeo.ecm.automation.client.cache.CacheBehavior;
import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
import org.nuxeo.ecm.automation.client.jaxrs.model.IdRef;
import org.nuxeo.ecm.automation.client.jaxrs.model.PathRef;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertiesHelper;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;

import android.content.Context;
import android.util.Log;

/**
 * See <a href="http://doc.nuxeo.com/x/mQAz">Nuxeo documentation on Content
 * Automation</a>
 */
public class NuxeoGardenProvider extends LocalGardenProvider {

    private static final String TAG = "NuxeoGardenProvider";

    String myToken;

    String myLogin;

    String myDeviceId;

    String myApp;

    // private static final long TIMEOUT = 10;

    protected NuxeoServerConfig nxConfig;

    protected NuxeoContext nuxeoContext;

    protected AndroidAutomationClient nuxeoClient;

    protected LazyUpdatableDocumentsList documentsList;

    public NuxeoGardenProvider(Context context) {
        super(context);
    }

    protected AndroidAutomationClient getNuxeoClient() {
        return NuxeoManager.getInstance().getNuxeoClient();
    }

    @Override
    public List<GardenInterface> getMyGardens() {
        // List<GardenInterface> myCachedGardens = super.getMyGardens();
        List<GardenInterface> myCachedGardens ;
        if (documentsList != null) {
            myCachedGardens = new ArrayList<GardenInterface>();
            documentsList.refreshAll();
            for (int i=0;i<=documentsList.getLoadedPageCount();i++){
//            for (Iterator<Document> iterator = documentsList.getIterator(); iterator.hasNext();) {
                Document documentGarden = documentsList.getDocument(i);
                GardenInterface garden = NuxeoGardenConvertor.convert(documentGarden);
                myCachedGardens.add(garden);
                Log.d(TAG, "documentsList=" + documentGarden.getId() + " / " + garden);
    }
//            return myCachedGardens;
        }else
            myCachedGardens = super.getMyGardens();

        return getMyNuxeoGardens(super.getMyGardens(), true);

        }

    /**
     * Returns either the list of remote gardens or the full list of gardens
     * with synchronization between local and
     * remote
     *
     * @param myLocalGardens can be null if not syncWithLocalGardens
     * @param syncWithLocalGardens whether to sync or not local and remote
     *            gardens
     * @return
     */
    protected List<GardenInterface> getMyNuxeoGardens(List<GardenInterface> myLocalGardens,
            final boolean syncWithLocalGardens) {
        List<GardenInterface> myGardens = new ArrayList<GardenInterface>();
        List<GardenInterface> remoteGardens = new ArrayList<GardenInterface>();

        try {
        Session session = getNuxeoClient().getSession();
        DocumentManager service = session.getAdapter(DocumentManager.class);
            // gardensWorkspaces = service.getChildren(wsRef);
            // Documents gardensWorkspaces =
            // service.query("SELECT * FROM Garden WHERE ecm:currentLifeCycleState <> 'deleted' ORDER BY dc:modified DESC");
            byte cacheParam = CacheBehavior.STORE;
            boolean refresh = true;
            if (refresh) {
                cacheParam = (byte) (cacheParam | CacheBehavior.FORCE_REFRESH);
                refresh = false;
            }
            Documents gardensWorkspaces = service.query(
                    "SELECT * FROM Garden WHERE ecm:currentLifeCycleState != \"deleted\"", null,
                    new String[] { "dc:modified true" }, "*", 0, 50, cacheParam);

            documentsList = gardensWorkspaces.asUpdatableDocumentsList();

            // TODO JC Documents gardensWorkspaces = service.query(nxql, queryParams, sortInfo, schemaList, page,
            // pageSize, cacheFlags);
            // documentsList = gardensWorkspaces.asUpdatableDocumentsList();
            for (Iterator<Document> iterator = gardensWorkspaces.iterator(); iterator.hasNext();) {
                Document gardenWorkspace = iterator.next();
                GardenInterface garden = NuxeoGardenConvertor.convert(gardenWorkspace);
                remoteGardens.add(garden);
                Log.d(TAG, "Document=" + gardenWorkspace.getId() + " / " + garden);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            // TODO check workaround need and consequences
            // remoteGardens = getMyLocalGardens();
            // cancel(false);
            // return myLocalGardens;
        }

        // Synchronize remote garden with local gardens
        for (GardenInterface remoteGarden : remoteGardens) {
            boolean found = false;
            for (GardenInterface localGarden : myLocalGardens) {
                if (remoteGarden.getUUID() != null && remoteGarden.getUUID().equals(localGarden.getUUID())) {
                    found = true;
                    break;
                }
            }
            if (found) { // local and remote => update local
                // TODO check if remote can be out of date
                // syncGardens(localGarden,remoteGarden);
                myGardens.add(super.updateGarden(remoteGarden));
            } else { // remote only => create local
                myGardens.add(createLocalGarden(remoteGarden));
            }
        }

        // Create remote garden when not exist remotely and remove local
        // garden if no more referenced online
        for (GardenInterface localGarden : myLocalGardens) {
            if (localGarden.getUUID() == null) { // local only without
                                                 // UUID => create
                                                 // remote
                myGardens.add(createNuxeoGarden(localGarden));
            } else {
                boolean found = false;
                for (GardenInterface remoteGarden : remoteGardens) {
                    if (remoteGarden.getUUID() != null && remoteGarden.getUUID().equals(localGarden.getUUID())) {
                        found = true;
                        break;
                    }
                }
                if (!found) { // local only with UUID -> delete local
                    removeLocalGarden(localGarden);
                }
            }
        }

        return myGardens;
    }

    @Override
    public GardenInterface createGarden(GardenInterface garden) {
        return createNuxeoGarden(createLocalGarden(garden));
    }

    protected GardenInterface createLocalGarden(GardenInterface garden) {
        Log.i(TAG, "createLocalGarden " + garden);

        return super.createGarden(garden);
    }

    protected GardenInterface createNuxeoGarden(GardenInterface localGarden) {
        Log.i(TAG, "createRemoteGarden " + localGarden);

        GardenInterface currentGarden = localGarden;
        Session session = getNuxeoClient().getSession();
        DocumentManager documentMgr = session.getAdapter(DocumentManager.class);
        // DeferredUpdateManager deferredUpdateMgr = getNuxeoClient().getDeferredUpdatetManager();

        Document createDocument;
        try {
            Document root = documentMgr.getUserHome();
            // createDocument = documentMgr.createDocument(home, "Garden", currentGarden.getLocality(), props);
            createDocument = new Document(root.getPath(), currentGarden.getLocality(), "Garden");

            OperationRequest createOperation = getNuxeoSession().newRequest("Document.Create").setHeader(
                    Constants.HEADER_NX_SCHEMAS, "*").set("type", "Garden");

            PropertyMap dirty = createDocument.getDirtyProperties();
            dirty.set("dc:title", currentGarden.getLocality());

            String dirtyString = PropertiesHelper.toStringProperties(dirty);

            createOperation.setInput(root).set("properties", dirtyString);
            if (createDocument.getName() != null) {
                createOperation.set("name", createDocument.getName());
            }
            documentsList.registerListener(new DocumentsListChangeListener() {

                @Override
                public void notifyContentChanged(int page) {
                    Document doc = documentsList.getDocument(page);
                    documentsList.getCurrentDocument();
                    documentsList.getCurrentPosition();
                    doc.getTitle();
                    Log.d(TAG, "notifyContentChanged on "+doc.getName()+" - "+doc.getId());
                }
            });
            documentsList.createDocument(createDocument, createOperation);

            documentsList.getCurrentDocument();
            if (createDocument != null) {
                localGarden.setUUID(createDocument.getPath());
                super.updateGarden(localGarden);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            // cancel(false);
        }

        return localGarden;
    }

    @Override
    public GardenInterface updateGarden(GardenInterface garden) {
        return updateNuxeoGarden(super.updateGarden(garden));
    }

    protected GardenInterface updateNuxeoGarden(final GardenInterface garden) {

        Log.i(TAG, "updateRemoteGarden " + garden);

        // TODO get document by id
        Session session = getNuxeoClient().getSession();
        PropertyMap props = new PropertyMap();
        props.set("dc:title", garden.getLocality());
        DocumentManager service = session.getAdapter(DocumentManager.class);

        try {
            Document updatedDocument = NuxeoGardenConvertor.convert(service.getUserHome().getPath(), garden);
            // TODO JC: documentsList.updateDocument(updatedDocument, updateOperation);
            documentsList.updateDocument(updatedDocument);
            // service.update(idRef, props);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            // cancel(false);
            return garden;
        }

        return garden;
    }

    @Override
    public int removeGarden(GardenInterface garden) {
        removeNuxeoGarden(garden);
        return removeLocalGarden(garden);
}

    protected int removeLocalGarden(GardenInterface garden) {
        Log.i(TAG, "removeLocalGarden " + garden);

        return super.removeGarden(garden);
    }

    protected void removeNuxeoGarden(final GardenInterface garden) {
        Log.i(TAG, "removeRemoteGarden " + garden);

        Session session = getNuxeoClient().getSession();
        DocumentManager service = session.getAdapter(DocumentManager.class);
        try {
            service.remove(new IdRef(garden.getUUID()));
            // documentsList.remove(updatedDocument);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            // cancel(false);
        }

    }
}
