import './globals.css';

export const metadata = {
  title: 'BugBoard26 - Gruppo 10',
  description: 'Piattaforma gestione issue',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="it">
      <body>{children}</body>
    </html>
  );
}