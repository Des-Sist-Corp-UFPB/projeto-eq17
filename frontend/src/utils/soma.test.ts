import { expect, test } from 'vitest';
import { soma } from './soma';

test('soma dois números', () => {
  expect(soma(1, 2)).toBe(3);
});
