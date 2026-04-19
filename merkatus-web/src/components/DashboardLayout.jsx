import DashboardNav from "./DashboardNav";

export default function DashboardLayout({ children }) {
  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <DashboardNav />

      {/* Main content area with sidebar offset */}
      <div className="md:ml-64 md:mt-16">
        <main className="p-4 md:p-8 max-w-7xl mx-auto">{children}</main>
      </div>
    </div>
  );
}
