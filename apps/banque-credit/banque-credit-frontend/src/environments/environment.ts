export const environment = {
  production: false,
  // ASSUMPTION: backend deployed as WAR on Tomcat with context path '/banque-credit'
  // and API base path '/api/v1' (spec-architecture-banque-credit.md §7).
  apiUrl: 'http://localhost:8080/banque-credit/api/v1'
};

