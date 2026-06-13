// Configuração da URL base da API
// Em desenvolvimento: aponta para localhost:8080 (configurado via .env.development ou VITE_API_URL)
// Em produção: aponta para a raiz do próprio servidor (mesma origem)
const BASE_URL = import.meta.env.VITE_API_URL || '';

interface RequestOptions {
  method?: string;
  body?: any;
  headers?: Record<string, string>;
  isFormUrlEncoded?: boolean;
}

export async function apiRequest<T = any>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, headers = {}, isFormUrlEncoded = false } = options;

  const url = `${BASE_URL}${endpoint}`;

  const fetchOptions: RequestInit = {
    method,
    // ESSENCIAL para autenticação baseada em Cookies (Session) através de CORS
    credentials: 'include', 
    headers: {
      ...headers,
    },
  };

  if (body) {
    if (body instanceof FormData) {
      fetchOptions.body = body;
    } else if (isFormUrlEncoded) {
      fetchOptions.headers = {
        ...fetchOptions.headers,
        'Content-Type': 'application/x-www-form-urlencoded',
      };
      fetchOptions.body = body;
    } else {
      fetchOptions.headers = {
        ...fetchOptions.headers,
        'Content-Type': 'application/json',
      };
      fetchOptions.body = JSON.stringify(body);
    }
  }

  const response = await fetch(url, fetchOptions);

  if (!response.ok) {
    let errorMessage = 'Ocorreu um erro no servidor';
    try {
      const errorData = await response.json();
      errorMessage = errorData.erro || errorData.mensagem || errorMessage;
    } catch {
      // Falha ao parsear JSON de erro
    }
    throw new Error(errorMessage);
  }

  // Se a resposta for 200 OK sem corpo (ex: logout, exclusão)
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return response.json() as Promise<T>;
  }

  return {} as T;
}

export const api = {
  get: <T>(endpoint: string, headers?: Record<string, string>) => 
    apiRequest<T>(endpoint, { method: 'GET', headers }),
    
  post: <T>(endpoint: string, body?: any, isFormUrlEncoded = false, headers?: Record<string, string>) => 
    apiRequest<T>(endpoint, { method: 'POST', body, isFormUrlEncoded, headers }),
    
  postMultipart: <T>(endpoint: string, formData: FormData, headers?: Record<string, string>) => 
    apiRequest<T>(endpoint, { method: 'POST', body: formData, headers }),
    
  put: <T>(endpoint: string, body?: any, headers?: Record<string, string>) => 
    apiRequest<T>(endpoint, { method: 'PUT', body, headers }),
    
  delete: <T>(endpoint: string, headers?: Record<string, string>) => 
    apiRequest<T>(endpoint, { method: 'DELETE', headers }),
};
