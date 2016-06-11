package at.ac.tuwien.big.we16.ue4.controller;

import at.ac.tuwien.big.we16.ue4.exception.InvalidCredentialsException;
import at.ac.tuwien.big.we16.ue4.exception.PasswordHashingException;
import at.ac.tuwien.big.we16.ue4.model.User;
import at.ac.tuwien.big.we16.ue4.service.AuthService;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void getLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //TODO: Edit (e.g. send back information about the currently logged in user)
        if (this.authService.isLoggedIn(request.getSession())) {
            sendUserDataback(request,response);
            return;
        }
        //TODO: send error back
        this.showLoginPage(request, response, false);
    }

    public void postLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, PasswordHashingException, ServletException {
    	//TODO: Try to log in and send back information about the currently logged in user
        if (this.authService.isLoggedIn(request.getSession())) {
            sendUserDataback(request,response);
            return;
        }
        try {
            this.authService.login(request.getSession(), request.getParameter("email"), request.getParameter("password"));
            sendUserDataback(request,response);
        } catch (InvalidCredentialsException e) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JSONObject obj = new JSONObject();
            try {
                obj.put("success",false);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            out.write(obj.toString());
            //TODO: send back error
        }
    }

    public void getLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	//TODO: Try to log out and send back information about the currently logged in user
        this.authService.logout(request.getSession());
        response.sendRedirect("/login");
    }

    //TODO: Should not be necessary any more
    private void showLoginPage(HttpServletRequest request, HttpServletResponse response, boolean error) throws ServletException, IOException {
        request.setAttribute("error", error);
        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }

    public void sendUserDataback(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
        if (this.authService.isLoggedIn(request.getSession())) {
            try{
                HttpSession session = request.getSession();
                User user = (User)session.getAttribute("user");
                user.getConvertedBalance();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                //JSONObject jsonObj = (JSONObject) JSONValue.parse(request.getParameter("para"));
                //System.out.println(jsonObj.get("message"));
                JSONObject obj = new JSONObject();
                obj.put("success",true);
                obj.put("name",user.getFullName());
                obj.put("balance",user.getConvertedBalance());
                obj.put("running",user.getRunningAuctionsCount());
                obj.put("lost",user.getLostAuctionsCount());
                obj.put("won",user.getWonAuctionsCount());
                out.write(obj.toString());
            }catch (JSONException e){
                e.printStackTrace();
                throw new ServletException(e.getMessage());
            }
            return;
        }
    }
}
