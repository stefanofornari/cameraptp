/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ste.ptp;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreePTPException {

    @Test
    public void constructors() {
        final String MSG = "MSG";
        final Throwable T = new Exception();
        final int CODE = Response.AccessDenied;

        PTPException e = new PTPException();

        then(e.getCause()).isNull();
        then(e.getMessage()).isSameAs("");
        then(e.getErrorCode()).isEqualTo(Response.Undefined);

        e = new PTPException(MSG);
        then(e).hasNoCause();
        then(e.getMessage()).isSameAs(MSG);
        then(e.getErrorCode()).isEqualTo(Response.Undefined);

        e = new PTPException(MSG, T);
        then(e).hasCause(T);
        then(e.getMessage()).isSameAs(MSG);
        then(e.getErrorCode()).isEqualTo(Response.Undefined);

        e = new PTPException(CODE);
        then(e).hasNoCause();
        then(e.getErrorCode()).isEqualTo(CODE);

        e = new PTPException(MSG, CODE);
        then(e).hasNoCause();
        then(e.getMessage()).isSameAs(MSG);
        then(e.getErrorCode()).isEqualTo(CODE);
    }

}
