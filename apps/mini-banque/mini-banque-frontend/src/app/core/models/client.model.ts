export interface Client {
  id: number;
  nom: string;
  email: string;
}

export interface CreateClientRequest {
  nom: string;
  email: string;
}
