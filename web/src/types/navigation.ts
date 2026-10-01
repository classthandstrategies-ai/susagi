export interface NavigationItem {
  name: string;
  href: string;
  icon: "home" | "shield" | "activity" | "users" | "settings" | "live";
  badge?: string;
  isPrimary: boolean;
}
