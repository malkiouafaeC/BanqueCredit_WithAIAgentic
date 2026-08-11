export const environment = {
  production: true,
  // ASSUMPTION: in production the Angular static build is served behind the same
  // Apache/Tomcat host as the backend WAR, under context path '/banque-credit'.
  apiUrl: '/banque-credit/api/v1'
};

