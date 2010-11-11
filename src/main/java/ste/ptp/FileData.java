// Copyright 2000 by David Brownell <dbrownell@users.sourceforge.net>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package ste.ptp;

import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Used with {@link BaselineInitiator#fillObject fillObject}, this writes
 * objects to files using a relatively small amount of in-memory buffering.
 * That reduces system resource requirements for working with large files
 * such as uncompressed TIF images supported by higher end imaging devices,
 * and provides a more even system I/O load.
 *
 * @see BaselineInitiator#fillObject
 *
 * @version $Id: FileData.java,v 1.2 2001/04/12 23:13:00 dbrownell Exp $
 * @author David Brownell
 */
public class FileData extends Data
{
    private FileOutputStream	out;

    // FIXME:  shouldn't be just "File" streams

    /**
     * Constructs a data object which fills the given underlying file.
     */
    public FileData (FileOutputStream o, NameFactory f)
    {
	super (f);
	out = o;
    }

    /**
     * Appends object data to the underlying file.
     */
    public void write (byte buf [], int off, int len)
    throws IOException
    {
	out.write (buf, off, len);
    }

    /**
     * Closes the underlying file.
     */
    public void close ()
    throws IOException
    {
	out.close ();
    }

    // we buffered nothing ... nothing to parse!

    final void parse ()
    {
    }
}

