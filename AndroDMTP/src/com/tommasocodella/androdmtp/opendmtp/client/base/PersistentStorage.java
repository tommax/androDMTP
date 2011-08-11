// ----------------------------------------------------------------------------
// Copyright 2006-2008, Martin D. Flynn
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  This class manages the persisten storage for properties used by the client
//  for the OpenDMTP protocol.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/11/03  Kiet Huynh
//      Include JavaDocs
// ----------------------------------------------------------------------------
package com.tommasocodella.androdmtp.opendmtp.client.base;

import java.util.Enumeration;
import java.util.Vector;

import com.tommasocodella.androdmtp.opendmtp.util.Log;

/**
* Provides mechanisms to save records to persistent storage
*/
public class PersistentStorage
    implements Props.AuxiliaryStore
{

    // ------------------------------------------------------------------------

    private static final String LOG_NAME = "STORE";

    // ------------------------------------------------------------------------

    private String storeName = null;
    //private RecordStore rcdStore = null;
    
    /**
    * PersistentStorage constructor
    * @param storeName The name of the storage.
    */
    public PersistentStorage(String storeName)
    {
        this.storeName = storeName;
    }

	@Override
	public boolean writeData(Vector rcds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vector readData() {
		// TODO Auto-generated method stub
		return null;
	}
    
    // ------------------------------------------------------------------------
    
    /**
    * Opens the storage.
    * @param reset If true, the current Recordstore will be deleted.
    * @return The open storage.
    * @throws RecordStoreException If errors occurred.
    * @throws RecordStoreFullException If errors occurred.
    * @throws RecordStoreNotFoundException If the RecordStore was not found.
    *//*
    private RecordStore open(boolean reset)
         throws RecordStoreException, RecordStoreFullException, RecordStoreNotFoundException
    {
        this.close();
        if (reset) {
            try {
                RecordStore.deleteRecordStore(this.storeName);
            } catch (Throwable t) {
                // ignore errors
            }
        }
        this.rcdStore = RecordStore.openRecordStore(this.storeName, true);
        return this.rcdStore;
    }
    */
    /**
    * Closes the storage.
    *//*
    private void close()
    {
        if (this.rcdStore != null) {
            try {
                this.rcdStore.closeRecordStore();
            } catch (Throwable t) {
                // ignore errors 
            }
            this.rcdStore = null;
        }
    }
*/
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    * Writes data to the persistent storage
    * @param rcds A container of records.
    * @return True of all records were written to disk successfully.
    *//*
    public boolean writeData(Vector rcds)
    {
        boolean rtn = false;
        try {
            RecordStore store = this.open(true);
            // Note: this operation should not be interrupted until complete
            // otherwise the persistent store will be in an indeterminate state.
            for (Enumeration rcdIter = rcds.elements(); rcdIter.hasMoreElements();) {
                byte b[] = null;
                Object obj = rcdIter.nextElement();
                if (obj instanceof String) {
                    b = ((String)obj).getBytes();
                } else
                if (obj instanceof byte[]) {
                    b = (byte[])obj;
                }
                if (b != null) {
                    store.addRecord(b, 0, b.length);
                }
            }
            rtn = true;
        } catch (Throwable t) {
            Log.error(LOG_NAME, "Writing RecordStore", t);
            rtn = false;
        } finally {
            this.close();
        }
        return rtn;
    }
    */
    /**
    * Reads records that were saved in persistent into a Vector container.
    * @return A Vector container of records.
    *//*
    public Vector readData()
    {
        Vector data = null;
        RecordEnumeration rcdIter = null;
        try {
            RecordStore store = this.open(false);
            rcdIter = store.enumerateRecords(null, null, false);
            data = new Vector();
            for (;rcdIter.hasNextElement();) {
                byte b[] = rcdIter.nextRecord();
                data.addElement((b != null)? new String(b) : null);
            }
        } catch (Throwable t) {
            Log.error(LOG_NAME, "Reading RecordStore", t);
            data = null;
        } finally {
            if (rcdIter != null) { rcdIter.destroy(); }
            this.close();
        }
        return data;
    }
    */
    // ------------------------------------------------------------------------

}
