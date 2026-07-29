import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api, apiRequest } from './api';

describe('api service', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('deve realizar requisição GET com sucesso', async () => {
    const mockData = { id: 1, nome: 'Casa Teste' };
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => mockData,
    });

    const data = await api.get('/api/casas');
    expect(fetch).toHaveBeenCalledWith('/api/casas', expect.objectContaining({
      method: 'GET',
      credentials: 'include',
    }));
    expect(data).toEqual(mockData);
  });

  it('deve realizar requisição POST enviando JSON', async () => {
    const payload = { nome: 'Nova Casa' };
    const responseData = { id: 2, ...payload };
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => responseData,
    });

    const data = await api.post('/api/casas', payload);
    expect(fetch).toHaveBeenCalledWith('/api/casas', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify(payload),
      headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
    }));
    expect(data).toEqual(responseData);
  });

  it('deve tratar erros da API extraindo a mensagem de erro em JSON', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: async () => ({ erro: 'Credenciais inválidas' }),
    });

    await expect(api.post('/api/auth/login', { username: 'test@email.com', password: '123' }))
      .rejects.toThrow('Credenciais inválidas');
  });

  it('deve tratar erro genérico quando o JSON de erro falhar', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: async () => { throw new Error('invalid json'); },
    });

    await expect(api.get('/api/erro'))
      .rejects.toThrow('Ocorreu um erro no servidor');
  });

  it('deve realizar requisição PUT e DELETE com sucesso', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({ status: 'ok' }),
    });

    await api.put('/api/casas/1', { nome: 'Atualizada' });
    expect(fetch).toHaveBeenCalledWith('/api/casas/1', expect.objectContaining({ method: 'PUT' }));

    await api.delete('/api/casas/1');
    expect(fetch).toHaveBeenCalledWith('/api/casas/1', expect.objectContaining({ method: 'DELETE' }));
  });
});
