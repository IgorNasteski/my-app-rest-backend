package io.javabrains.springsecurityjwt.controller;

import io.javabrains.springsecurityjwt.dao.TokenUserRepository;
import io.javabrains.springsecurityjwt.dao.UserRepository;
import io.javabrains.springsecurityjwt.model.TokenUser;
import io.javabrains.springsecurityjwt.model.UserEntity;
import io.javabrains.springsecurityjwt.msg.*;
import io.javabrains.springsecurityjwt.security.JwtUtil;
import io.javabrains.springsecurityjwt.services.MyUserDetailsService;
import io.javabrains.springsecurityjwt.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class HelloWorldController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenUserRepository tokenUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }

    @GetMapping("/hello2")
    public String hello2(){
        return "Hello World 2";
    }

    //uzima username i password od usera kad se uloguje, a vraca JWT useru(onda kad user svaki put proba da ode na neki endpoint on ce nam slati JWT taj i mi
    //uporedjujemo jel to onaj JWT koji smo mu mi poslali nakon uspesnog logina)
    //KAD ODEM NA OVU PUTANJU preko postmana(prosledim u request username i password "foo" koji sam zakucao u MyUserDetailsService klasi) kao odgovor cu dobiti
    //"jwt": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJleHAiOjE2MDk2NTIzNDIsImlhdCI6MTYwOTYxNjM0Mn0.N1yZumhngvZp57rIrmLSCKDOJvRWk4O85UeaJyKkHc8"
    //NAKON TOGA, svaki put kada bude user hteo da ide na neki endpoint, morace u hederu da ima polje:
    //Authorization     (dodam "Bearer" i nalepim vrednost "jwt"-a koji sam poslao useru nakon /authenticateLogin(gde sam kreirao token i poslao mu)
    //KAKO DA KAZEM SPRING SECURITY-JU DA SVAKI PUT PRESRETNE REQUEST I POGLEDA HEDER? TAKO STO CU KREIRATI FILTER KLASU KOJA CE DA PRESRETNE REQUEST OD USERA I
    //POGLEDA TOKEN U HEDERU(Bearer + jwt) I AKO JE TO ONAJ KOJI SAM SLAO USERU NAKON /authenticateLogin ONDA GA PUSTI JER JE TO ON. KAD KREIRAM TU FILTER KLASU, SAMO DODAM
    //TU FILTER KLASU U SECURITY CONFIG KLASI SecurityConfigurer
    /*@PostMapping("/authenticateLogin")
    public ResponseEntity createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest)throws Exception{
        try {
            authenticationManager.authenticate(     //proveravam jel user validan
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        }
        catch(BadCredentialsException e){
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);//generise/napravi mi token kad u ovu metodu prosledim usera

        return ResponseEntity.ok(new AuthenticationResponse(jwt));  //vraca 200 ok i pakuje token u String u response-u(ja useru saljem ovo prvi put, posle on meni
                                                                    //mora pri svakom requestu za bilo koji endpoint da salje u hederu "Authorization" "Bearer ..."
                                                                    //tj slace mi taj token i ja cu da ga posle proverim jel isti
    }*/

    @PostMapping("/authenticateLogin")
    public AuthenticationResponse createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest)throws Exception{

        System.out.println("STIGAO ZAHTEV IZ MVC FRONT ENDA, AUTENTICATION REQUEST. USERNAME: " + authenticationRequest.getUsername() + ", PASSWORD: " +
                                                                                                authenticationRequest.getPassword());

        try {
            authenticationManager.authenticate(     //proveravam jel user validan
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        }
        catch(BadCredentialsException e){
            String incorrectMessage = "Incorrect username or password";
            System.out.println(incorrectMessage);
            return new AuthenticationResponse(incorrectMessage);
            //throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        System.out.println("USER DETAILS: PASSWORD" + userDetails.getUsername() + " PASSWORD " + userDetails.getPassword());
        final String jwt = jwtTokenUtil.generateToken(userDetails);//generise/napravi mi token kad u ovu metodu prosledim usera
        System.out.println("JWT: " + jwt);

        //saving token from user who is loggin in - in db
        UserEntity userEntityFromDb = userRepository.findByUsername(authenticationRequest.getUsername());
        TokenUser tokenUser = tokenUserRepository.findById(userEntityFromDb.getTokenUser().getId()).orElse(null);
        tokenUser.setUserEntity(userEntityFromDb);
        tokenUser.setJwt(jwt);
        tokenUserRepository.save(tokenUser);

        //return ResponseEntity.ok(new AuthenticationResponse(jwt));  //vraca 200 ok i pakuje token u String u response-u(ja useru saljem ovo prvi put, posle on meni
        //return new AuthenticationResponse("adad");
        return new AuthenticationResponse(jwt);
        //mora pri svakom requestu za bilo koji endpoint da salje u hederu "Authorization" "Bearer ..."
        //tj slace mi taj token i ja cu da ga posle proverim jel isti
    }

    /* Returning simple string, in case if user is entered proper info for sign up, and if that is successful and entering user in db, return to mvc api
      * string which is sending the user to page which contains text about successful signing up, and a link to login page */
    @PostMapping("/signUp")
    public String signUp(@RequestBody UserSignUpRequest userSignUpRequest){

        System.out.println("user sign up request username " + userSignUpRequest.getUsername());
        System.out.println("user sign up request password " + userSignUpRequest.getPassword());
        System.out.println("user sign up request email " + userSignUpRequest.getEmail());

        if(userSignUpRequest.getUsername() == null || userSignUpRequest.getPassword() == null || userSignUpRequest.getEmail() == null){
            return "error-registration";
        } else if(userService.isUserWithThisUsernameExistsInDb(userSignUpRequest.getUsername())) {    //isUserWithThisUsernameExistsInDb.isPresent()
            return "username-already-taken";
        } else if(userService.isUserWithThisEmailExistsInDb(userSignUpRequest.getEmail())) {          //isUserWithThisEmailExistsInDb.isPresent()
            return "email-already-taken";
        } else{

            userService.saveUserAndSetEmptyTokenAfterSignUp(userSignUpRequest.getUsername(), userSignUpRequest.getPassword(), userSignUpRequest.getEmail());
            /*TokenUser tokenUser = new TokenUser();
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(userSignUpRequest.getUsername());

            //userEntity.setPassword(userSignUpRequest.getPassword());
            userEntity.setPassword(passwordEncoder.encode(userSignUpRequest.getPassword()));
            System.out.println("ENKODIRAN PASSWORD   " + passwordEncoder.encode(userSignUpRequest.getPassword()));

            userEntity.setEmail(userSignUpRequest.getEmail());
            tokenUser.setJwt("");
            tokenUser.setUserEntity(userEntity);
            userEntity.setTokenUser(tokenUser);

            userRepository.save(userEntity);
            tokenUserRepository.save(tokenUser);*/

            return "success-registration";
        }
    }


    /*public Users createUser(@RequestBody Users userEntity) {
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        return userRepo.save(userEntity);
    }*/




    @PostMapping("/obicanString")
    public String vratiResponseStringMvcApiju(){
        System.out.println("koristio");
        return "hello-world";
    }

    @PostMapping("/retrievePasswordAndSendItToMvc")
    public UserPasswordResponse sendUserPasswordToMvc(@RequestBody UserUsernameRequest userUsernameRequest){
        UserEntity userEntity = userRepository.findByUsername(userUsernameRequest.getUsername());
        return new UserPasswordResponse(userEntity.getPassword());
    }

    /*@GetMapping("/probaZaSlanjeRequestObjektaIHedera")
    public String proba(@RequestBody AuthenticationRequest authenticationRequest){
        System.out.println("DOBIO I HEDER I OBJEKAT! AUTHENTICATION REQUEST USERNAME I PASS:: " + authenticationRequest.getUsername() + " " + authenticationRequest.getPassword());
        AuthenticationResponse authenticationResponse = new AuthenticationResponse("hello-world");

        return authenticationResponse.getJwt();
    }*/

    //ne radi, moram popraviti
    @PostMapping("/probaZaSlanjeRequestObjektaIHedera")
    public AuthenticationResponse proba(/*@RequestBody AuthenticationRequest authenticationRequest, */@RequestBody HttpEntity<?> httpEntity){
        //System.out.println("DOBIO I HEDER I OBJEKAT! AUTHENTICATION REQUEST USERNAME I PASS:: " + authenticationRequest.getUsername() + " " + authenticationRequest.getPassword());
        AuthenticationResponse authenticationResponse = new AuthenticationResponse("hello-world");
        AuthenticationRequest authenticationRequest1 = (AuthenticationRequest) httpEntity.getBody();
        System.out.println("DOBIO I HEDER I OBJEKAT! AUTHENTICATION REQUEST USERNAME I PASS:: " + authenticationRequest1.getUsername() + " " + authenticationRequest1.getPassword());
        return authenticationResponse;
    }

}
