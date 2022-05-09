package pz.gr3.serwer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class CustomAuth {
    public static Integer getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null)
            return null;
        Integer id = (Integer)session.getAttribute("USER_ID");
        if(id == null)
            throw new NullPointerException();
        return (Integer)session.getAttribute("USER_ID");
    }
}
