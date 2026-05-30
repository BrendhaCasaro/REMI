# REMI UI — Implementation Roadmap

## Pages

| Route | Page | Description |
|-------|------|-------------|
| `/login` | Login | Username + password form, stores JWT token |
| `/medias` | Media List | Table of uploaded files, upload, download, delete |
| `/users` | User Management | Table + CRUD dialogs for users |
| `/nodes` | Node Management | Table + CRUD dialogs for nodes |

## Layer Structure

```
app/
├── lib/
│   ├── api.ts          — API client interface + real HTTP
│   ├── mock.ts         — mock data + mock adapter
│   ├── types.ts        — shared type definitions
│   └── utils.ts        — cn() helper
├── components/
│   └── ui/             — shadcn components
├── routes/
│   ├── login.tsx
│   ├── medias.tsx
│   ├── users.tsx
│   └── nodes.tsx
├── hooks/
│   └── useAuth.ts      — auth context / hook
└── root.tsx
```

## Implementation Order

### Step 1 — API client + types + mock data
Files: `types.ts`, `api.ts`, `mock.ts`
- All shared type definitions matching backend contracts
- Mock adapter returning typed fake data
- Real API client with auth header (ready for when backend is live)

### Step 2 — Login page
File: `routes/login.tsx`
- Username + password form with shadcn Input + Button
- Mock: accepts any credentials, stores fake token
- Route guard for authenticated pages

### Step 3 — Media list page
File: `routes/medias.tsx`
- Table with name, createdAt, download + delete actions
- Upload button with file picker
- Mock upload adds to local list
- Download button with placeholder

### Step 4 — User management page
File: `routes/users.tsx`
- Table with ID, username, role columns
- Create/edit/delete dialogs

### Step 5 — Node management page
File: `routes/nodes.tsx`
- Table with URL, capacity, status columns
- Create/edit/delete dialogs

### Step 6 — Polish
- Loading skeletons, empty states
- Toast notifications (sonner)
- Responsive layout
- Error boundaries
