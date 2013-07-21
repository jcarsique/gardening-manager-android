package org.gots.garden.provider.nuxeo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gots.garden.GardenInterface;
import org.gots.garden.provider.local.LocalGardenProvider;
import org.gots.nuxeo.NuxeoManager;
import org.nuxeo.android.config.NuxeoServerConfig;
import org.nuxeo.android.context.NuxeoContext;
import org.nuxeo.android.documentprovider.LazyUpdatableDocumentsList;
import org.nuxeo.android.repository.DocumentManager;
import org.nuxeo.ecm.automation.client.android.AndroidAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.Session;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
import org.nuxeo.ecm.automation.client.jaxrs.model.IdRef;
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
    public GardenInterface createGarden(GardenInterface garden) {
        return createRemoteGarden(createLocalGarden(garden));
    }

    protected GardenInterface createLocalGarden(GardenInterface garden) {
        return super.createGarden(garden);
    }

    protected GardenInterface createRemoteGarden(final GardenInterface localGarden) {
        Log.i(TAG, "createRemoteGarden " + localGarden);

        GardenInterface currentGarden = localGarden;
        Session session = getNuxeoClient().getSession();
        PropertyMap props = new PropertyMap();
        props.set("dc:title", currentGarden.getLocality());
        DocumentManager documentMgr = session.getAdapter(DocumentManager.class);
        // DeferredUpdateManager deferredUpdateMgr = getNuxeoClient().getDeferredUpdatetManager();

        // TODO use service.getUserHome()
        // DocRef wsRef = new DocRef("/default-domain/UserWorkspaces/" +
        // myLogin);
        Document createDocument;
        try {
            Document home = documentMgr.getUserHome();
            createDocument = documentMgr.createDocument(home, "Garden", currentGarden.getLocality(), props);
            // TODO JC: documentsList.createDocument(newDocument, createOperation);
            // return createDocument;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            // cancel(false);
            return localGarden;
        }
        if (createDocument != null) {
            localGarden.setUUID(createDocument.getId());
            super.updateGarden(localGarden);
        }
        // new AsyncTask<GardenInterface, Integer, Document>() {
        // @Override
        // protected void onPreExecute() {
        // super.onPreExecute();
        // // TODO show loading... icon
        // }
        //
        // @Override
        // protected Document doInBackground(GardenInterface... params) {
        // Log.i(TAG, "createRemoteGarden " + localGarden);
        //
        // GardenInterface currentGarden = params[0];
        // Session session = getNuxeoClient().getSession();
        // PropertyMap props = new PropertyMap();
        // props.set("dc:title", currentGarden.getLocality());
        // DocumentManager service = session.getAdapter(DocumentManager.class);
        // // TODO use service.getUserHome()
        // // DocRef wsRef = new DocRef("/default-domain/UserWorkspaces/" +
        // // myLogin);
        //
        // try {
        // Document home = service.getUserHome();
        //
        // Document createDocument = service.createDocument(home,
        // "Garden", currentGarden.getLocality(), props);
        // return createDocument;
        //
        // } catch (Exception e) {
        // Log.e(TAG, e.getMessage(), e);
        // cancel(false);
        // return null;
        // }
        // }
        //
        // @Override
        // protected void onPostExecute(Document newGarden) {
        // if (newGarden != null) {
        // super.onPostExecute(newGarden);
        // localGarden.setUUID(newGarden.getId());
        // updateLocalGarden(localGarden);
        // }
        // // TODO show ok icon
        // }
        //
        // @Override
        // protected void onCancelled(Document result) {
        // // TODO show error icon
        // };
        // }.execute(localGarden);
        return localGarden;
    }

    @Override
    public List<GardenInterface> getMyGardens() {
        List<GardenInterface> myLocalGardens = super.getMyGardens();
        // if (documentsList != null) {
        // documentsList.refreshAll();
        // } else {
        return getMyRemoteGardens(myLocalGardens, true);
        // }
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
    protected List<GardenInterface> getMyRemoteGardens(List<GardenInterface> myLocalGardens,
            final boolean syncWithLocalGardens) {
        List<GardenInterface> myGardens = new ArrayList<GardenInterface>();
        List<GardenInterface> remoteGardens = new ArrayList<GardenInterface>();

        Session session = getNuxeoClient().getSession();
        DocumentManager service = session.getAdapter(DocumentManager.class);
        try {
            // gardensWorkspaces = service.getChildren(wsRef);
            Documents gardensWorkspaces = service.query("SELECT * FROM Garden WHERE ecm:currentLifeCycleState <> 'deleted' ORDER BY dc:modified DESC");
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
                myGardens.add(createRemoteGarden(localGarden));
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

        // AsyncTask<Void, Integer, List<GardenInterface>> task = new
        // AsyncTask<Void, Integer, List<GardenInterface>>() {
        // @Override
        // protected void onPreExecute() {
        // super.onPreExecute();
        // // TODO show loading... icon
        // }
        //
        // @Override
        // protected List<GardenInterface> doInBackground(Void... none) {
        // List<GardenInterface> remoteGardens = new
        // ArrayList<GardenInterface>();
        // Session session = getNuxeoClient().getSession();
        // DocumentManager service = session.getAdapter(DocumentManager.class);
        // // TODO use service.getUserHome()
        // Documents gardensWorkspaces = null;
        //
        // try {
        // // gardensWorkspaces = service.getChildren(wsRef);
        // gardensWorkspaces =
        // service.query("SELECT * FROM Garden WHERE ecm:currentLifeCycleState <> 'deleted' ORDER BY dc:modified DESC");
        // for (Iterator<Document> iterator = gardensWorkspaces.iterator();
        // iterator.hasNext();) {
        // Document gardenWorkspace = iterator.next();
        // Log.d(TAG, "Document=" + gardenWorkspace.getId());
        // GardenInterface garden =
        // NuxeoGardenConvertor.convert(gardenWorkspace);
        // remoteGardens.add(garden);
        // }
        // } catch (Exception e) {
        // Log.e(TAG, e.getMessage(), e);
        // // TODO check workaround need and consequences
        // // remoteGardens = getMyLocalGardens();
        // cancel(false);
        // }
        // return remoteGardens;
        // }
        //
        // @Override
        // protected void onPostExecute(List<GardenInterface> remoteGardens) {
        // super.onPostExecute(remoteGardens);
        // if (!syncWithLocalGardens) {
        // return;
        // }
        //
        // // Synchronize remote garden with local gardens
        // for (GardenInterface remoteGarden : remoteGardens) {
        // boolean found = false;
        // for (GardenInterface localGarden : myLocalGardens) {
        // if (remoteGarden.getUUID() != null
        // && remoteGarden.getUUID().equals(
        // localGarden.getUUID())) {
        // found = true;
        // break;
        // }
        // }
        // if (found) { // local and remote => update local
        // // TODO check if remote can be out of date
        // // syncGardens(localGarden,remoteGarden);
        // myGardens.add(updateLocalGarden(remoteGarden));
        // } else { // remote only => create local
        // myGardens.add(createLocalGarden(remoteGarden));
        // }
        // }
        //
        // // Create remote garden when not exist remotly and remote local
        // // garden if no more reference online
        // for (GardenInterface localGarden : myLocalGardens) {
        // if (localGarden.getUUID() == null) { // local only without
        // // UUID => create
        // // remote
        //
        // GardenInterface createRemoteGarden = createRemoteGarden(localGarden);
        //
        // myGardens.add(createRemoteGarden);
        // } else {
        // boolean found = false;
        // for (GardenInterface remoteGarden : remoteGardens) {
        // if (remoteGarden.getUUID() != null
        // && remoteGarden.getUUID().equals(
        // localGarden.getUUID())) {
        // found = true;
        // break;
        // }
        // }
        // if (!found) { // local only with UUID -> delete local
        // removeLocalGarden(localGarden);
        // }
        // }
        // }
        // // TODO show ok icon
        // };
        //
        // @Override
        // protected void onCancelled(List<GardenInterface> remoteGardens) {
        // // TODO show error icon
        // }
        //
        // }.execute();
        // if (!syncWithLocalGardens) {
        // try {
        // return task.get();
        // } catch (InterruptedException e) {
        // Log.e(TAG, e.getMessage());
        // } catch (ExecutionException e) {
        // Log.e(TAG, e.getMessage(), e);
        // }
        // }
        return myGardens;
    }

    @Override
    public int removeGarden(GardenInterface garden) {
        removeRemoteGarden(garden);
        return removeLocalGarden(garden);
    }

    protected int removeLocalGarden(GardenInterface garden) {
        Log.i(TAG, "removeLocalGarden " + garden);

        return super.removeGarden(garden);
    }

    protected void removeRemoteGarden(final GardenInterface garden) {
        Log.i(TAG, "removeRemoteGarden " + garden);

        Session session = getNuxeoClient().getSession();
        DocumentManager service = session.getAdapter(DocumentManager.class);
        try {
            service.remove(new IdRef(garden.getUUID()));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            // cancel(false);
        }
        // return null;
        // new AsyncTask<Void, Integer, Void>() {
        //
        // @Override
        // protected void onPreExecute() {
        // super.onPreExecute();
        // }
        //
        // @Override
        // protected Void doInBackground(Void... none) {
        // Log.i(TAG, "removeRemoteGarden " + garden);
        //
        // Session session = getNuxeoClient().getSession();
        // DocumentManager service = session.getAdapter(DocumentManager.class);
        // try {
        // service.remove(new IdRef(garden.getUUID()));
        // } catch (Exception e) {
        // Log.e(TAG, e.getMessage(), e);
        // cancel(false);
        // }
        // return null;
        // }
        //
        // @Override
        // protected void onPostExecute(Void none) {
        // super.onPostExecute(none);
        // }
        //
        // @Override
        // protected void onCancelled(Void none) {
        // // TODO show error icon
        // };
        // }.execute();
    }

    @Override
    public GardenInterface updateGarden(GardenInterface garden) {
        return updateRemoteGarden(super.updateGarden(garden));
    }

    protected GardenInterface updateRemoteGarden(final GardenInterface garden) {

        Log.i(TAG, "updateRemoteGarden " + garden);

        // TODO get document by id
        IdRef idRef = new IdRef(garden.getUUID());
        Session session = getNuxeoClient().getSession();
        PropertyMap props = new PropertyMap();
        props.set("dc:title", garden.getLocality());
        DocumentManager service = session.getAdapter(DocumentManager.class);
        // TODO JC: documentsList.updateDocument(updatedDocument, updateOperation);
        try {
            service.update(idRef, props);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            // cancel(false);
            return garden;
        }
        //
        // new AsyncTask<Void, Integer, Document>() {
        //
        // @Override
        // protected void onPreExecute() {
        // super.onPreExecute();
        //
        // }
        //
        // @Override
        // protected Document doInBackground(Void... none) {
        // Log.i(TAG, "updateRemoteGarden " + garden);
        //
        // // TODO get document by id
        // IdRef idRef = new IdRef(garden.getUUID());
        // Session session = getNuxeoClient().getSession();
        // PropertyMap props = new PropertyMap();
        // props.set("dc:title", garden.getLocality());
        // DocumentManager service = session.getAdapter(DocumentManager.class);
        // try {
        // return service.update(idRef, props);
        // } catch (Exception e) {
        // Log.e(TAG, e.getMessage(), e);
        // cancel(false);
        // return null;
        // }
        // }
        //
        // @Override
        // protected void onPostExecute(Document newGarden) {
        // super.onPostExecute(newGarden);
        //
        // }
        //
        // @Override
        // protected void onCancelled(Document result) {
        // // TODO show error icon
        // };
        // }.execute();
        return garden;
    }
}
