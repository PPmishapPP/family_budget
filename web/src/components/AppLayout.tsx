import { AppShell, Burger, Group, NavLink, Title } from '@mantine/core'
import { useDisclosure } from '@mantine/hooks'
import { IconCalendarStats, IconHistory, IconMovie, IconWallet } from '@tabler/icons-react'
import { Link, Outlet, useLocation } from 'react-router-dom'

const NAV_ITEMS = [
  { to: '/payments', label: 'План платежей', icon: IconCalendarStats },
  { to: '/history', label: 'История платежей', icon: IconHistory },
  { to: '/media', label: 'Фильмы и сериалы', icon: IconMovie },
] as const

export function AppLayout() {
  const [opened, { toggle, close }] = useDisclosure()
  const { pathname } = useLocation()

  return (
    <AppShell
      header={{ height: 56 }}
      navbar={{ width: 240, breakpoint: 'sm', collapsed: { mobile: !opened } }}
      padding="md"
    >
      <AppShell.Header>
        <Group h="100%" px="md" gap="sm">
          <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" aria-label="Меню" />
          <IconWallet size={22} />
          <Title order={4}>Family Budget</Title>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="sm">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            component={Link}
            to={to}
            label={label}
            leftSection={<Icon size={18} />}
            active={pathname === to || pathname.startsWith(`${to}/`)}
            onClick={close}
            mb={4}
          />
        ))}
      </AppShell.Navbar>

      <AppShell.Main>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  )
}
