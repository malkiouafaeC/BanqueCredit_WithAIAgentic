import { DemandeCreditSummary } from '../demandes/demande-credit.model';

/** Client (spec-architecture-banque-credit.md §7.2 ClientDto). */
export interface Client {
  id: number;
  nom: string;
  email: string | null;
  revenuMensuel: number;
  chargesMensuelles: number;
  situationProfessionnelle: string | null;
  createdAt: string;
}

/** Request body for POST /clients (RG-BANK-01). */
export interface ClientCreate {
  nom: string;
  email?: string | null;
  revenuMensuel: number;
  chargesMensuelles: number;
  situationProfessionnelle?: string | null;
}

/** Response body for GET /clients/{id}: client + associated demandes (US-006). */
export interface ClientDetail extends Client {
  demandes: DemandeCreditSummary[];
}

