package de.litexo;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Filter for routing all 404-Errors to angular and trigger its router *

 */
@WebFilter(urlPatterns = "/*")
public class AngularRouteFilter extends HttpFilter {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(".*[.][a-zA-Z\\d]+");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        chain.doFilter(request, response);

        if (response.getStatus() == 404) {
            String path = request.getRequestURI().substring(request.getContextPath().length()).replaceAll("[/]+$", "");
            if (!FILE_NAME_PATTERN.matcher(path).matches()) {
                // We could not find the resource, i.e. it is not anything known to the server (i.e. it is not a REST
                // endpoint or a servlet), and does not look like a file so try handling it in the front-end routes
                // and reset the response status code to 200.
                response.reset();
                request.getRequestDispatcher("/").forward(request, response);
            }
        }
    }
}
