/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ste.ptp;

import junit.framework.TestCase;

/**
 *
 * @author ste
 */
public class PTPExceptionTest extends TestCase {
    
    public PTPExceptionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructors() {
        final String MSG = "MSG";
        final Throwable T = new Exception();
        final int CODE = Response.AccessDenied;
        
        PTPException e = new PTPException();

        assertNull(e.getCause());
        assertSame("", e.getMessage());
        assertEquals(Response.Undefined, e.getErrorCode());

        e = new PTPException(MSG);
        assertNull(e.getCause());
        assertSame(MSG, e.getMessage());
        assertEquals(Response.Undefined, e.getErrorCode());

        e = new PTPException(MSG, T);
        assertEquals(T, e.getCause());
        assertSame(MSG, e.getMessage());
        assertEquals(Response.Undefined, e.getErrorCode());

        e = new PTPException(CODE);
        assertNull(e.getCause());
        assertEquals(CODE, e.getErrorCode());

        e = new PTPException(MSG, CODE);
        assertNull(e.getCause());
        assertSame(MSG, e.getMessage());
        assertEquals(CODE, e.getErrorCode());

    }

}
