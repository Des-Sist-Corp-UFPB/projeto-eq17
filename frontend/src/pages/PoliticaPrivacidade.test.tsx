import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import PoliticaPrivacidade from './PoliticaPrivacidade';

describe('PoliticaPrivacidade Component', () => {
  it('deve renderizar o título e seções da Política de Privacidade e LGPD', () => {
    render(
      <MemoryRouter>
        <PoliticaPrivacidade />
      </MemoryRouter>
    );

    expect(screen.getByRole('heading', { level: 1, name: 'Política de Privacidade' })).toBeInTheDocument();
    expect(screen.getByText(/Termos de Uso e Proteção de Dados Pessoais/i)).toBeInTheDocument();
  });
});
