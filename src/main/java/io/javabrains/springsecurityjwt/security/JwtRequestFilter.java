package io.javabrains.springsecurityjwt.security;

import io.javabrains.springsecurityjwt.services.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//OVO JE FILTER KOJI CE DA PRESRETNE SVAKI REQUEST KOJI STIZE OD USERA JEDNOM, KAKO BI PREGLEDAO HEDER I PROVERIO DA LI JE TO TAJ JWT(token) KOJI SAM MU SLAO
//NAKON /authentication A KOJI TREBA DA MI STIGNE SVAKI PUT KAD USER BUDE ISAO NA BILO KOJI ENDPOINT
@Component
public class JwtRequestFilter extends OncePerRequestFilter{

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final String authenticationHeader = httpServletRequest.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if(authenticationHeader != null && authenticationHeader.startsWith("Bearer")){
            jwt = authenticationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if(jwtUtil.validateToken(jwt, userDetails)){
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(//sve u ovom if-u je standardni flow
                        userDetails, null, userDetails.getAuthorities());                       //koji spring radi u pozadini, bukvalno smo isto napisali
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}
