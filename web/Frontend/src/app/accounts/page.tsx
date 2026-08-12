'use client';

import { useState, useEffect } from 'react';
import accountService, { AccountItem, UnlinkedEmployee, PermissionItem, RoleDetailItem } from '@/services/accountService';

// Permission display name mapper in Vietnamese
const PERMISSION_DISPLAY_NAMES: Record<string, { label: string; desc: string }> = {
  DASHBOARD_VIEW: { label: 'Xem tổng quan', desc: 'Xem trang tổng quan, thống kê doanh thu và hoạt động chung' },
  
  INVENTORY_VIEW: { label: 'Xem hàng hóa', desc: 'Xem danh sách và tồn kho của sản phẩm' },
  INVENTORY_CREATE: { label: 'Thêm hàng hóa', desc: 'Tạo sản phẩm mới, định lượng công thức' },
  INVENTORY_EDIT: { label: 'Sửa hàng hóa', desc: 'Cập nhật thông tin, giá bán và công thức sản phẩm' },
  INVENTORY_DELETE: { label: 'Xóa hàng hóa', desc: 'Xóa sản phẩm khỏi danh mục quản lý' },
  
  SALES_VIEW: { label: 'Xem đơn bán hàng', desc: 'Xem lịch sử hóa đơn bán lẻ' },
  SALES_CREATE: { label: 'Tạo đơn bán hàng', desc: 'Thực hiện bán hàng và lập hóa đơn bán lẻ' },
  SALES_EDIT: { label: 'Sửa đơn bán hàng', desc: 'Chỉnh sửa thông tin hóa đơn bán lẻ đã lập' },
  SALES_DELETE: { label: 'Xóa đơn bán hàng', desc: 'Hủy/Xóa hóa đơn bán lẻ đã lập' },
  
  PURCHASE_VIEW: { label: 'Xem đơn nhập hàng', desc: 'Xem danh sách phiếu nhập hàng từ nhà cung cấp' },
  PURCHASE_CREATE: { label: 'Tạo đơn nhập hàng', desc: 'Tạo phiếu nhập kho, tăng số lượng tồn kho' },
  PURCHASE_EDIT: { label: 'Sửa đơn nhập hàng', desc: 'Sửa thông tin phiếu nhập kho' },
  PURCHASE_DELETE: { label: 'Xóa đơn nhập hàng', desc: 'Hủy/Xóa phiếu nhập kho' },
  
  PARTNERS_VIEW: { label: 'Xem đối tác & nhân sự', desc: 'Xem danh sách khách hàng, nhà cung cấp và nhân sự' },
  PARTNERS_CREATE: { label: 'Thêm đối tác & nhân sự', desc: 'Thêm mới thông tin đối tác hoặc nhân sự' },
  PARTNERS_EDIT: { label: 'Sửa đối tác & nhân sự', desc: 'Sửa thông tin đối tác hoặc nhân viên' },
  PARTNERS_DELETE: { label: 'Xóa đối tác & nhân sự', desc: 'Xóa thông tin đối tác hoặc nhân viên khỏi danh sách' },
  
  EXPENSE_VIEW: { label: 'Xem sổ thu chi', desc: 'Xem lịch sử phiếu thu, phiếu chi tiền mặt' },
  EXPENSE_CREATE: { label: 'Tạo phiếu thu chi', desc: 'Lập phiếu thu tiền hoặc phiếu chi tiền mặt' },
  EXPENSE_EDIT: { label: 'Sửa phiếu thu chi', desc: 'Sửa thông tin phiếu thu chi đã lập' },
  EXPENSE_DELETE: { label: 'Xóa phiếu thu chi', desc: 'Hủy/Xóa phiếu thu chi' },
  
  ACTIVITY_LOGS_VIEW: { label: 'Xem nhật ký hệ thống', desc: 'Xem nhật ký hoạt động thay đổi dữ liệu của nhân viên' },
  EXCEL_CONFIG: { label: 'Cấu hình Excel', desc: 'Cài đặt và ánh xạ tệp Excel nhập xuất dữ liệu' },
  ACCOUNTS_MANAGE: { label: 'Quản lý tài khoản', desc: 'Tối cao: Cấp tài khoản, đổi mật khẩu, phân quyền' }
};

const PERMISSION_ORDER = [
  'DASHBOARD_VIEW',
  'INVENTORY_VIEW',
  'INVENTORY_CREATE',
  'INVENTORY_EDIT',
  'INVENTORY_DELETE',
  'SALES_VIEW',
  'SALES_CREATE',
  'SALES_EDIT',
  'SALES_DELETE',
  'PURCHASE_VIEW',
  'PURCHASE_CREATE',
  'PURCHASE_EDIT',
  'PURCHASE_DELETE',
  'PARTNERS_VIEW',
  'PARTNERS_CREATE',
  'PARTNERS_EDIT',
  'PARTNERS_DELETE',
  'EXPENSE_VIEW',
  'EXPENSE_CREATE',
  'EXPENSE_EDIT',
  'EXPENSE_DELETE',
  'ACTIVITY_LOGS_VIEW',
  'EXCEL_CONFIG',
  'ACCOUNTS_MANAGE'
];

const sortPermissions = <T extends string | PermissionItem>(list: T[]): T[] => {
  const getIndex = (item: T): number => {
    const key = typeof item === 'string' ? item : item.tenQuyen;
    const idx = PERMISSION_ORDER.indexOf(key);
    return idx === -1 ? Infinity : idx;
  };
  return [...list].sort((a, b) => getIndex(a) - getIndex(b));
};

const PERMISSION_GROUPS = [
  {
    title: 'Tổng quan',
    icon: 'dashboard',
    prefixes: ['DASHBOARD_']
  },
  {
    title: 'Hàng hóa',
    icon: 'inventory',
    prefixes: ['INVENTORY_']
  },
  {
    title: 'Bán hàng',
    icon: 'point_of_sale',
    prefixes: ['SALES_']
  },
  {
    title: 'Nhập hàng',
    icon: 'local_shipping',
    prefixes: ['PURCHASE_']
  },
  {
    title: 'Đối tác & nhân viên',
    icon: 'groups',
    prefixes: ['PARTNERS_']
  },
  {
    title: 'Nhật ký thu chi',
    icon: 'account_balance_wallet',
    prefixes: ['EXPENSE_']
  },
  {
    title: 'Nhật ký hoạt động',
    icon: 'history',
    prefixes: ['ACTIVITY_LOGS_']
  },
  {
    title: 'Cấu hình Excel',
    icon: 'table_chart',
    prefixes: ['EXCEL_']
  },
  {
    title: 'Quản lý tài khoản',
    icon: 'manage_accounts',
    prefixes: ['ACCOUNTS_']
  }
];

interface GroupedPermissionSection {
  title: string;
  icon: string;
  items: PermissionItem[];
}

const groupPermissions = (perms: PermissionItem[]): GroupedPermissionSection[] => {
  const groups: GroupedPermissionSection[] = PERMISSION_GROUPS.map(g => ({
    title: g.title,
    icon: g.icon,
    items: []
  }));

  const others: PermissionItem[] = [];

  perms.forEach(perm => {
    let matched = false;
    for (let i = 0; i < PERMISSION_GROUPS.length; i++) {
      const g = PERMISSION_GROUPS[i];
      if (g.prefixes.some(pfx => perm.tenQuyen.startsWith(pfx))) {
        groups[i].items.push(perm);
        matched = true;
        break;
      }
    }
    if (!matched) {
      others.push(perm);
    }
  });

  const finalGroups = groups.filter(g => g.items.length > 0);
  if (others.length > 0) {
    finalGroups.push({
      title: 'Khác',
      icon: 'more_horiz',
      items: others
    });
  }
  return finalGroups;
};



export default function AccountsPage() {
  const [activeTab, setActiveTab] = useState<'accounts' | 'roles'>('accounts');
  
  // Data States
  const [accounts, setAccounts] = useState<AccountItem[]>([]);
  const [roles, setRoles] = useState<string[]>([]);
  const [rolesDetails, setRolesDetails] = useState<RoleDetailItem[]>([]);
  const [permissions, setPermissions] = useState<PermissionItem[]>([]);
  const [unlinkedEmployees, setUnlinkedEmployees] = useState<UnlinkedEmployee[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [rolesSearchQuery, setRolesSearchQuery] = useState('');

  // Modal States - Account
  const [isCreateAccountOpen, setIsCreateAccountOpen] = useState(false);
  const [isEditAccountOpen, setIsEditAccountOpen] = useState(false);
  
  // Modal States - Role
  const [isCreateRoleOpen, setIsCreateRoleOpen] = useState(false);
  const [isEditRoleOpen, setIsEditRoleOpen] = useState(false);
  
  const [modalError, setModalError] = useState<string | null>(null);

  // Form States - Account Create
  const [createUsername, setCreateUsername] = useState('');
  const [createPassword, setCreatePassword] = useState('');
  const [createEmployeeId, setCreateEmployeeId] = useState<number | ''>('');
  const [createAccountRoles, setCreateAccountRoles] = useState<string[]>([]);

  // Form States - Account Edit
  const [editingAccount, setEditingAccount] = useState<AccountItem | null>(null);
  const [editAccountStatus, setEditAccountStatus] = useState('ACTIVE');
  const [editAccountPassword, setEditAccountPassword] = useState('');
  const [editAccountRoles, setEditAccountRoles] = useState<string[]>([]);

  // Form States - Role Create
  const [createRoleName, setCreateRoleName] = useState('');
  const [createRoleDesc, setCreateRoleDesc] = useState('');
  const [createRolePerms, setCreateRolePerms] = useState<string[]>([]);

  // Form States - Role Edit
  const [editingRole, setEditingRole] = useState<RoleDetailItem | null>(null);
  const [editRoleName, setEditRoleName] = useState('');
  const [editRoleDesc, setEditRoleDesc] = useState('');
  const [editRolePerms, setEditRolePerms] = useState<string[]>([]);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [accList, roleList, roleDetailList, permList, empList] = await Promise.all([
        accountService.getAccounts(),
        accountService.getRoles(),
        accountService.getRolesDetails(),
        accountService.getPermissions(),
        accountService.getUnlinkedEmployees(),
      ]);
      setAccounts(accList);
      setRoles(roleList);
      setRolesDetails(roleDetailList);
      setPermissions(sortPermissions(permList));
      setUnlinkedEmployees(empList);
    } catch (err) {
      console.error('Error loading account and role management data:', err);
    } finally {
      setLoading(false);
    }
  };

  // --- ACCOUNTS ACTIONS ---
  const handleCreateAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);

    if (!createUsername.trim() || !createPassword.trim() || createEmployeeId === '') {
      setModalError('Vui lòng điền đầy đủ tên đăng nhập, mật khẩu và chọn nhân viên.');
      return;
    }

    if (createAccountRoles.length === 0) {
      setModalError('Vui lòng chọn ít nhất một vai trò/nhóm quyền.');
      return;
    }

    try {
      await accountService.createAccount({
        username: createUsername.trim(),
        password: createPassword.trim(),
        nhanVienId: Number(createEmployeeId),
        roles: createAccountRoles
      });
      setCreateUsername('');
      setCreatePassword('');
      setCreateEmployeeId('');
      setCreateAccountRoles([]);
      setIsCreateAccountOpen(false);
      loadData();
    } catch (err: any) {
      setModalError(err.message || 'Lỗi khi tạo tài khoản.');
    }
  };

  const handleEditAccount = (account: AccountItem) => {
    setEditingAccount(account);
    setEditAccountStatus(account.trangThai);
    setEditAccountPassword('');
    setEditAccountRoles(account.roles);
    setModalError(null);
    setIsEditAccountOpen(true);
  };

  const handleUpdateAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);
    if (!editingAccount) return;

    if (editAccountRoles.length === 0) {
      setModalError('Vui lòng chọn ít nhất một vai trò/nhóm quyền.');
      return;
    }

    try {
      await accountService.updateAccount(editingAccount.id, {
        trangThai: editAccountStatus,
        roles: editAccountRoles,
        password: editAccountPassword.trim() || undefined
      });
      setIsEditAccountOpen(false);
      loadData();
    } catch (err: any) {
      setModalError(err.message || 'Lỗi khi cập nhật tài khoản.');
    }
  };

  const handleDeleteAccount = async (id: number, username: string) => {
    if (username === 'admin') {
      alert('Không thể xóa tài khoản admin hệ thống.');
      return;
    }
    if (!window.confirm(`Bạn có chắc chắn muốn xóa tài khoản "${username}" không?`)) {
      return;
    }
    try {
      await accountService.deleteAccount(id);
      loadData();
    } catch (err: any) {
      alert(`Không thể xóa tài khoản: ${err.message || 'Lỗi kết nối'}`);
    }
  };

  // --- ROLES & PERMISSIONS ACTIONS ---
  const handleCreateRole = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);

    if (!createRoleName.trim()) {
      setModalError('Tên vai trò không được để trống.');
      return;
    }

    try {
      await accountService.createRole({
        tenVaiTro: createRoleName.trim(),
        moTa: createRoleDesc.trim() || undefined,
        quyens: createRolePerms
      });
      setCreateRoleName('');
      setCreateRoleDesc('');
      setCreateRolePerms([]);
      setIsCreateRoleOpen(false);
      loadData();
    } catch (err: any) {
      setModalError(err.message || 'Lỗi khi tạo vai trò.');
    }
  };

  const handleEditRole = (role: RoleDetailItem) => {
    setEditingRole(role);
    setEditRoleName(role.tenVaiTro);
    setEditRoleDesc(role.moTa || '');
    setEditRolePerms(role.quyens);
    setModalError(null);
    setIsEditRoleOpen(true);
  };

  const handleUpdateRole = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);
    if (!editingRole) return;

    try {
      await accountService.updateRole(editingRole.id, {
        tenVaiTro: editRoleName.trim(),
        moTa: editRoleDesc.trim(),
        quyens: editRolePerms
      });
      setIsEditRoleOpen(false);
      loadData();
    } catch (err: any) {
      setModalError(err.message || 'Lỗi khi cập nhật vai trò.');
    }
  };

  const handleDeleteRole = async (id: number, roleName: string) => {
    const defaultRoles = ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_CASHIER', 'ROLE_BARISTA', 'ROLE_WAITER'];
    if (defaultRoles.includes(roleName)) {
      alert('Không thể xóa vai trò mặc định của hệ thống.');
      return;
    }

    if (!window.confirm(`Bạn có chắc chắn muốn xóa nhóm quyền/vai trò "${roleName}" không?`)) {
      return;
    }

    try {
      await accountService.deleteRole(id);
      loadData();
    } catch (err: any) {
      alert(`Không thể xóa vai trò: ${err.message || 'Vai trò đang được gán cho một số tài khoản.'}`);
    }
  };

  // Helper toggle functions
  const togglePermissionInList = (permName: string, isCreate: boolean) => {
    const list = isCreate ? createRolePerms : editRolePerms;
    const setList = isCreate ? setCreateRolePerms : setEditRolePerms;

    if (list.includes(permName)) {
      setList(list.filter((p) => p !== permName));
    } else {
      setList([...list, permName]);
    }
  };

  const toggleAccountRoleInList = (roleName: string, isCreate: boolean) => {
    const list = isCreate ? createAccountRoles : editAccountRoles;
    const setList = isCreate ? setCreateAccountRoles : setEditAccountRoles;

    if (list.includes(roleName)) {
      setList(list.filter((r) => r !== roleName));
    } else {
      setList([...list, roleName]);
    }
  };

  // Filter lists
  const filteredAccounts = accounts.filter((acc) => {
    const query = searchQuery.toLowerCase().trim();
    return (
      acc.username.toLowerCase().includes(query) ||
      acc.tenNhanVien.toLowerCase().includes(query)
    );
  });

  const filteredRoles = rolesDetails.filter((r) => {
    const query = rolesSearchQuery.toLowerCase().trim();
    return (
      r.tenVaiTro.toLowerCase().includes(query) ||
      (r.moTa && r.moTa.toLowerCase().includes(query))
    );
  });

  const getRoleBadgeColor = (roleName: string) => {
    switch (roleName) {
      case 'ROLE_ADMIN':
        return 'bg-error/15 text-error border-error/20';
      case 'ROLE_MANAGER':
        return 'bg-primary/15 text-primary border-primary/20';
      case 'ROLE_CASHIER':
        return 'bg-success/15 text-success border-success/20';
      case 'ROLE_BARISTA':
        return 'bg-warning/15 text-warning border-warning/20';
      default:
        return 'bg-secondary/15 text-secondary border-secondary/20';
    }
  };

  const cleanRoleName = (name: string) => name.replace('ROLE_', '');

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Top Header Controls with Tabs */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-white/10 pb-2 flex-shrink-0">
        <div className="flex flex-wrap items-center gap-6">
          <h2 className="text-xl font-bold text-white tracking-tight">
            {activeTab === 'accounts' ? 'Quản lý Tài khoản' : 'Nhóm quyền & Vai trò'}
          </h2>
          
          {/* Tabs Navigation */}
          <div className="flex bg-white/5 p-1 rounded-lg gap-1">
            <button
              onClick={() => { setActiveTab('accounts'); setSearchQuery(''); }}
              className={`px-4 py-1.5 rounded-md text-xs font-semibold uppercase tracking-wider transition-colors cursor-pointer ${
                activeTab === 'accounts' ? 'bg-primary text-on-primary font-bold' : 'text-on-surface-variant hover:text-white'
              }`}
            >
              Tài khoản
            </button>
            <button
              onClick={() => { setActiveTab('roles'); setRolesSearchQuery(''); }}
              className={`px-4 py-1.5 rounded-md text-xs font-semibold uppercase tracking-wider transition-colors cursor-pointer ${
                activeTab === 'roles' ? 'bg-primary text-on-primary font-bold' : 'text-on-surface-variant hover:text-white'
              }`}
            >
              Vai trò / Nhóm quyền
            </button>
          </div>
        </div>

        {activeTab === 'accounts' ? (
          <button
            onClick={() => {
              setModalError(null);
              setCreateUsername('');
              setCreatePassword('');
              setCreateEmployeeId(unlinkedEmployees.length > 0 ? unlinkedEmployees[0].id : '');
              setCreateAccountRoles([]);
              setIsCreateAccountOpen(true);
            }}
            className="bg-primary text-on-primary px-5 py-2 rounded-lg font-semibold flex items-center gap-2 glow-button transition-all active:scale-95 cursor-pointer text-xs"
          >
            <span className="material-symbols-outlined text-base">person_add</span>
            <span>Cấp tài khoản mới</span>
          </button>
        ) : (
          <button
            onClick={() => {
              setModalError(null);
              setCreateRoleName('');
              setCreateRoleDesc('');
              setCreateRolePerms([]);
              setIsCreateRoleOpen(true);
            }}
            className="bg-primary text-on-primary px-5 py-2 rounded-lg font-semibold flex items-center gap-2 glow-button transition-all active:scale-95 cursor-pointer text-xs"
          >
            <span className="material-symbols-outlined text-base">security</span>
            <span>Tạo vai trò mới</span>
          </button>
        )}
      </div>


      {/* TAB CONTENT 1: ACCOUNTS LIST */}
      {activeTab === 'accounts' && (
        <div className="flex-1 flex flex-col min-h-0 glass-card rounded-xl overflow-hidden border border-white/5 bg-white/1 mt-1 animate-in fade-in duration-200">
          <div className="p-3 border-b border-border-glass flex justify-between items-center flex-shrink-0">
            <span className="text-white font-semibold text-xs">Danh sách tài khoản đăng nhập</span>
            <div className="relative w-64">
              <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
                search
              </span>
              <input
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full bg-surface-lowest border border-border-glass rounded-lg py-1.5 pl-8 pr-3 text-xs focus:ring-1 focus:ring-primary/50 transition-all text-white outline-none"
                placeholder="Tìm kiếm tài khoản, nhân viên..."
                type="text"
              />
            </div>
          </div>

          <div className="flex-1 overflow-auto">
            {loading ? (
              <div className="h-full flex items-center justify-center text-xs text-on-surface-variant font-semibold">
                Đang tải danh sách tài khoản...
              </div>
            ) : filteredAccounts.length === 0 ? (
              <div className="p-12 text-center text-xs text-on-surface-variant font-semibold">
                Không tìm thấy tài khoản nào phù hợp.
              </div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead className="sticky top-0 z-10 bg-[#131929] shadow-[0_1px_0_0_rgba(255,255,255,0.08)]">
                  <tr className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider border-b border-border-glass bg-[#131929]">
                    <th className="px-4 py-3 w-16 text-center">ID</th>
                    <th className="px-4 py-3">Tên đăng nhập</th>
                    <th className="px-4 py-3">Nhân viên liên kết</th>
                    <th className="px-4 py-3">Nhóm vai trò gán</th>
                    <th className="px-4 py-3">Trạng thái</th>
                    <th className="px-4 py-3 text-center w-28">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-glass">
                  {filteredAccounts.map((account) => (
                    <tr key={account.id} className="hover:bg-white/5 transition-colors group">
                      <td className="px-4 py-3 font-mono text-xs text-secondary text-center">#{account.id}</td>
                      <td className="px-4 py-3 text-xs font-bold text-white">{account.username}</td>
                      <td className="px-4 py-3 text-xs text-white">
                        {account.tenNhanVien}
                        {account.nhanVienId && (
                          <span className="text-[10px] text-on-surface-variant ml-1.5 font-mono">
                            (NV-{account.nhanVienId})
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex flex-wrap gap-1">
                          {account.roles.map((role) => (
                            <span
                              key={role}
                              className={`px-1.5 py-0.5 rounded text-[9px] font-bold border ${getRoleBadgeColor(
                                role
                              )}`}
                            >
                              {cleanRoleName(role)}
                            </span>
                          ))}
                        </div>
                      </td>
                      <td className="px-4 py-3 text-xs">
                        {account.trangThai === 'ACTIVE' ? (
                          <span className="flex items-center gap-1.5 text-success font-semibold">
                            <span className="h-1.5 w-1.5 rounded-full bg-success animate-pulse" />
                            Hoạt động
                          </span>
                        ) : (
                          <span className="flex items-center gap-1.5 text-error font-semibold">
                            <span className="h-1.5 w-1.5 rounded-full bg-error" />
                            Đang khóa
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <div className="flex items-center justify-center gap-1.5">
                          <button
                            onClick={() => handleEditAccount(account)}
                            className="p-1 hover:bg-primary/10 rounded text-on-surface-variant hover:text-primary transition-colors cursor-pointer inline-flex items-center"
                            title="Sửa vai trò & Trạng thái"
                          >
                            <span className="material-symbols-outlined text-base">edit</span>
                          </button>
                          <button
                            onClick={() => handleDeleteAccount(account.id, account.username)}
                            disabled={account.username === 'admin'}
                            className="p-1 hover:bg-error/10 rounded text-on-surface-variant hover:text-error transition-colors cursor-pointer inline-flex items-center disabled:opacity-30 disabled:pointer-events-none"
                            title="Xóa tài khoản"
                          >
                            <span className="material-symbols-outlined text-base">delete</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* TAB CONTENT 2: ROLES & PERMISSIONS */}
      {activeTab === 'roles' && (
        <div className="flex-1 flex flex-col min-h-0 glass-card rounded-xl overflow-hidden border border-white/5 bg-white/1 mt-1 animate-in fade-in duration-200">
          <div className="p-3 border-b border-border-glass flex justify-between items-center flex-shrink-0">
            <span className="text-white font-semibold text-xs">Danh sách Nhóm Vai trò và Quyền hạn chi tiết</span>
            <div className="relative w-64">
              <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
                search
              </span>
              <input
                value={rolesSearchQuery}
                onChange={(e) => setRolesSearchQuery(e.target.value)}
                className="w-full bg-surface-lowest border border-border-glass rounded-lg py-1.5 pl-8 pr-3 text-xs focus:ring-1 focus:ring-primary/50 transition-all text-white outline-none"
                placeholder="Tìm tên vai trò, mô tả..."
                type="text"
              />
            </div>
          </div>

          <div className="flex-1 overflow-auto">
            {loading ? (
              <div className="h-full flex items-center justify-center text-xs text-on-surface-variant font-semibold">
                Đang tải danh sách vai trò...
              </div>
            ) : filteredRoles.length === 0 ? (
              <div className="p-12 text-center text-xs text-on-surface-variant font-semibold">
                Không tìm thấy vai trò nào phù hợp.
              </div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead className="sticky top-0 z-10 bg-[#131929] shadow-[0_1px_0_0_rgba(255,255,255,0.08)]">
                  <tr className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider border-b border-border-glass bg-[#131929]">
                    <th className="px-4 py-3 w-40">Mã Vai trò</th>
                    <th className="px-4 py-3 w-64">Mô tả chức năng</th>
                    <th className="px-4 py-3">Danh sách Quyền (Permissions) được cấp</th>
                    <th className="px-4 py-3 text-center w-28">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-glass">
                  {filteredRoles.map((role) => {
                    const isSystemRole = ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_CASHIER', 'ROLE_BARISTA', 'ROLE_WAITER'].includes(role.tenVaiTro);
                    return (
                      <tr key={role.id} className="hover:bg-white/5 transition-colors group">
                        <td className="px-4 py-3 text-xs font-bold text-white">
                          <div className="flex items-center gap-1.5">
                            <span className="font-mono text-secondary">{cleanRoleName(role.tenVaiTro)}</span>
                            {isSystemRole && (
                              <span className="text-[8px] bg-white/10 px-1 py-0.5 rounded text-on-surface-variant/80 border border-white/5">
                                Mặc định
                              </span>
                            )}
                          </div>
                        </td>
                        <td className="px-4 py-3 text-xs text-on-surface-variant font-medium">{role.moTa || 'Chưa cung cấp mô tả.'}</td>
                        <td className="px-4 py-3">
                          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-w-2xl">
                            {role.quyens.length === 0 ? (
                              <span className="text-[10px] text-error font-medium italic">Không được cấp quyền nào</span>
                            ) : (
                              sortPermissions(role.quyens).map((perm) => {
                                const display = PERMISSION_DISPLAY_NAMES[perm] || { label: perm, desc: 'Chưa cập nhật mô tả' };
                                return (
                                  <div key={perm} className="p-2 rounded bg-white/5 border border-white/5 flex flex-col justify-center">
                                    <span className="text-[10px] font-bold text-white">{display.label}</span>
                                    <span className="text-[9px] text-on-surface-variant/80 mt-0.5 leading-snug">{display.desc}</span>
                                  </div>
                                );
                              })
                            )}
                          </div>
                        </td>
                        <td className="px-4 py-3 text-center">
                          <div className="flex items-center justify-center gap-1.5">
                            <button
                              onClick={() => handleEditRole(role)}
                              className="p-1 hover:bg-primary/10 rounded text-on-surface-variant hover:text-primary transition-colors cursor-pointer inline-flex items-center"
                              title="Cấu hình danh sách quyền"
                            >
                              <span className="material-symbols-outlined text-base">tune</span>
                            </button>
                            <button
                              onClick={() => handleDeleteRole(role.id, role.tenVaiTro)}
                              disabled={isSystemRole}
                              className="p-1 hover:bg-error/10 rounded text-on-surface-variant hover:text-error transition-colors cursor-pointer inline-flex items-center disabled:opacity-30 disabled:pointer-events-none"
                              title="Xóa vai trò"
                            >
                              <span className="material-symbols-outlined text-base">delete</span>
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* --- MODALS SECTION --- */}

      {/* CREATE ACCOUNT MODAL */}
      {isCreateAccountOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4 bg-background/30 backdrop-blur-lg animate-in fade-in duration-200">
          <div className="absolute inset-0" onClick={() => setIsCreateAccountOpen(false)}></div>
          <div className="glass-card w-full max-w-2xl rounded-2xl p-8 relative z-50 animate-in zoom-in-95 duration-250">
            <button
              className="absolute right-5 top-5 text-on-surface-variant hover:text-white cursor-pointer"
              onClick={() => setIsCreateAccountOpen(false)}
            >
              <span className="material-symbols-outlined text-lg">close</span>
            </button>
            <h3 className="text-md font-bold text-primary mb-4">Cấp tài khoản đăng nhập mới</h3>

            {modalError && (
              <div className="p-3 mb-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl">
                ⚠️ {modalError}
              </div>
            )}

            <form onSubmit={handleCreateAccount} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Nhân viên sở hữu</label>
                {unlinkedEmployees.length === 0 ? (
                  <div className="p-3 text-xs bg-warning/5 border border-warning/20 rounded-lg text-warning font-semibold">
                    ⚠️ Tất cả nhân viên hiện tại đều đã được cấp tài khoản. Vui lòng tạo thêm nhân viên trước.
                  </div>
                ) : (
                  <select
                    required
                    value={createEmployeeId}
                    onChange={(e) => setCreateEmployeeId(e.target.value ? Number(e.target.value) : '')}
                    className="w-full bg-[#131929] border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-primary/50 cursor-pointer"
                  >
                    <option value="">-- Chọn nhân viên --</option>
                    {unlinkedEmployees.map((emp) => (
                      <option key={emp.id} value={emp.id}>
                        {emp.tenNhanVien} ({emp.vaiTro || 'Chưa định nghĩa'})
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Tên đăng nhập</label>
                  <input
                    required
                    value={createUsername}
                    onChange={(e) => setCreateUsername(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:ring-1 focus:ring-primary/50 outline-none"
                    placeholder="Nhập username..."
                    type="text"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Mật khẩu</label>
                  <input
                    required
                    value={createPassword}
                    onChange={(e) => setCreatePassword(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:ring-1 focus:ring-primary/50 outline-none"
                    placeholder="Mật khẩu khởi tạo..."
                    type="password"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Gán Nhóm vai trò (Có thể chọn nhiều)</label>
                <div className="grid grid-cols-2 gap-2 p-3 bg-[#131929] border border-border-glass rounded-xl max-h-48 overflow-y-auto">
                  {rolesDetails.map((role) => (
                    <label key={role.id} className="flex items-start gap-2.5 p-2 rounded-lg border border-white/5 bg-white/1 hover:bg-white/5 transition-colors cursor-pointer select-none">
                      <input
                        type="checkbox"
                        checked={createAccountRoles.includes(role.tenVaiTro)}
                        onChange={() => toggleAccountRoleInList(role.tenVaiTro, true)}
                        className="rounded border-white/20 bg-surface-lowest text-primary focus:ring-0 cursor-pointer h-3.5 w-3.5 mt-0.5"
                      />
                      <div className="min-w-0">
                        <span className="text-xs font-bold text-white block">{cleanRoleName(role.tenVaiTro)}</span>
                        <p className="text-[9px] text-on-surface-variant/80 mt-0.5 leading-snug">{role.moTa || 'Nhóm vai trò mặc định.'}</p>
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  disabled={unlinkedEmployees.length === 0}
                  className="flex-1 bg-primary text-on-primary font-bold py-2.5 rounded-lg text-xs hover:scale-[1.01] active:scale-95 transition-all cursor-pointer disabled:opacity-30 disabled:pointer-events-none"
                >
                  Cấp tài khoản
                </button>
                <button
                  type="button"
                  onClick={() => setIsCreateAccountOpen(false)}
                  className="flex-1 bg-white/5 border border-border-glass text-white font-bold py-2.5 rounded-lg text-xs hover:bg-white/10 transition-all cursor-pointer text-center"
                >
                  Đóng lại
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT ACCOUNT MODAL */}
      {isEditAccountOpen && editingAccount && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4 bg-background/30 backdrop-blur-lg animate-in fade-in duration-200">
          <div className="absolute inset-0" onClick={() => setIsEditAccountOpen(false)}></div>
          <div className="glass-card w-full max-w-2xl rounded-2xl p-8 relative z-50 animate-in zoom-in-95 duration-250">
            <button
              className="absolute right-5 top-5 text-on-surface-variant hover:text-white cursor-pointer"
              onClick={() => setIsEditAccountOpen(false)}
            >
              <span className="material-symbols-outlined text-lg">close</span>
            </button>
            <h3 className="text-md font-bold text-primary mb-4">Chỉnh sửa tài khoản: {editingAccount.username}</h3>

            {modalError && (
              <div className="p-3 mb-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl">
                ⚠️ {modalError}
              </div>
            )}

            <form onSubmit={handleUpdateAccount} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Trạng thái hoạt động</label>
                  <select
                    value={editAccountStatus}
                    onChange={(e) => setEditAccountStatus(e.target.value)}
                    className="w-full bg-[#131929] border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-primary/50 cursor-pointer"
                  >
                    <option value="ACTIVE">Hoạt động (ACTIVE)</option>
                    <option value="INACTIVE">Khóa tài khoản (INACTIVE)</option>
                  </select>
                </div>
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Đặt lại mật khẩu</label>
                  <input
                    value={editAccountPassword}
                    onChange={(e) => setEditAccountPassword(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:ring-1 focus:ring-primary/50 outline-none"
                    placeholder="Bỏ trống nếu không đổi..."
                    type="password"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Vai trò / Nhóm quyền (Có thể chọn nhiều)</label>
                <div className="grid grid-cols-2 gap-2 p-3 bg-[#131929] border border-border-glass rounded-xl max-h-48 overflow-y-auto">
                  {rolesDetails.map((role) => (
                    <label key={role.id} className="flex items-start gap-2.5 p-2 rounded-lg border border-white/5 bg-white/1 hover:bg-white/5 transition-colors cursor-pointer select-none">
                      <input
                        type="checkbox"
                        checked={editAccountRoles.includes(role.tenVaiTro)}
                        onChange={() => toggleAccountRoleInList(role.tenVaiTro, false)}
                        className="rounded border-white/20 bg-surface-lowest text-primary focus:ring-0 cursor-pointer h-3.5 w-3.5 mt-0.5"
                      />
                      <div className="min-w-0">
                        <span className="text-xs font-bold text-white block">{cleanRoleName(role.tenVaiTro)}</span>
                        <p className="text-[9px] text-on-surface-variant/80 mt-0.5 leading-snug">{role.moTa || 'Nhóm vai trò mặc định.'}</p>
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  className="flex-1 bg-primary text-on-primary font-bold py-2.5 rounded-lg text-xs hover:scale-[1.01] active:scale-95 transition-all cursor-pointer"
                >
                  Lưu thay đổi
                </button>
                <button
                  type="button"
                  onClick={() => setIsEditAccountOpen(false)}
                  className="flex-1 bg-white/5 border border-border-glass text-white font-bold py-2.5 rounded-lg text-xs hover:bg-white/10 transition-all cursor-pointer text-center"
                >
                  Hủy bỏ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* CREATE ROLE MODAL */}
      {isCreateRoleOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4 bg-background/30 backdrop-blur-lg animate-in fade-in duration-200">
          <div className="absolute inset-0" onClick={() => setIsCreateRoleOpen(false)}></div>
          <div className="glass-card w-full max-w-4xl rounded-2xl p-8 relative z-50 animate-in zoom-in-95 duration-250 flex flex-col max-h-[90vh]">
            <button
              className="absolute right-5 top-5 text-on-surface-variant hover:text-white cursor-pointer"
              onClick={() => setIsCreateRoleOpen(false)}
            >
              <span className="material-symbols-outlined text-lg">close</span>
            </button>
            <h3 className="text-md font-bold text-primary mb-4 flex-shrink-0">Tạo Nhóm vai trò / Quyền mới</h3>

            {modalError && (
              <div className="p-3 mb-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl flex-shrink-0">
                ⚠️ {modalError}
              </div>
            )}

            <form onSubmit={handleCreateRole} className="space-y-4 flex-1 flex flex-col min-h-0">
              <div className="grid grid-cols-2 gap-4 flex-shrink-0">
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Tên nhóm vai trò</label>
                  <input
                    required
                    value={createRoleName}
                    onChange={(e) => setCreateRoleName(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:ring-1 focus:ring-primary/50 outline-none"
                    placeholder="Ví dụ: ROLE_MARKETING"
                    type="text"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Mô tả vai trò</label>
                  <input
                    value={createRoleDesc}
                    onChange={(e) => setCreateRoleDesc(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:ring-1 focus:ring-primary/50 outline-none"
                    placeholder="Ví dụ: Nhân viên Marketing, không thao tác kho..."
                    type="text"
                  />
                </div>
              </div>

              <div className="space-y-1.5 flex-1 flex flex-col min-h-0">
                <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant flex-shrink-0">Tích chọn các quyền được phép thực hiện</label>
                <div className="flex-1 overflow-y-auto border border-border-glass rounded-xl p-3 bg-[#131929] space-y-4">
                  {groupPermissions(permissions).map((group) => (
                    <div key={group.title} className="p-3 bg-white/5 border border-white/5 rounded-xl space-y-3">
                      <div className="flex items-center gap-2 border-b border-white/10 pb-1.5 mb-1">
                        <span className="material-symbols-outlined text-xs text-primary">{group.icon}</span>
                        <span className="text-[10px] font-bold text-white uppercase tracking-wider">{group.title}</span>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {group.items.map((perm) => {
                          const display = PERMISSION_DISPLAY_NAMES[perm.tenQuyen] || { label: perm.tenQuyen, desc: perm.moTa };
                          return (
                            <label
                              key={perm.id}
                              className="flex items-start gap-2.5 p-2 rounded-lg border border-white/5 bg-white/1 hover:bg-white/5 transition-colors cursor-pointer select-none"
                            >
                              <input
                                type="checkbox"
                                checked={createRolePerms.includes(perm.tenQuyen)}
                                onChange={() => togglePermissionInList(perm.tenQuyen, true)}
                                className="rounded border-white/20 bg-surface-lowest text-primary focus:ring-0 cursor-pointer h-3.5 w-3.5 mt-0.5"
                              />
                              <div className="min-w-0">
                                <p className="text-xs font-bold text-white">{display.label}</p>
                                <p className="text-[9px] text-on-surface-variant mt-0.5 leading-tight">{display.desc}</p>
                              </div>
                            </label>
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex gap-3 pt-3 flex-shrink-0">
                <button
                  type="submit"
                  className="flex-1 bg-primary text-on-primary font-bold py-2.5 rounded-lg text-xs hover:scale-[1.01] active:scale-95 transition-all cursor-pointer"
                >
                  Tạo vai trò
                </button>
                <button
                  type="button"
                  onClick={() => setIsCreateRoleOpen(false)}
                  className="flex-1 bg-white/5 border border-border-glass text-white font-bold py-2.5 rounded-lg text-xs hover:bg-white/10 transition-all cursor-pointer text-center"
                >
                  Hủy bỏ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT ROLE MODAL */}
      {isEditRoleOpen && editingRole && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4 bg-background/30 backdrop-blur-lg animate-in fade-in duration-200">
          <div className="absolute inset-0" onClick={() => setIsEditRoleOpen(false)}></div>
          <div className="glass-card w-full max-w-4xl rounded-2xl p-8 relative z-50 animate-in zoom-in-95 duration-250 flex flex-col max-h-[90vh]">
            <button
              className="absolute right-5 top-5 text-on-surface-variant hover:text-white cursor-pointer"
              onClick={() => setIsEditRoleOpen(false)}
            >
              <span className="material-symbols-outlined text-lg">close</span>
            </button>
            <h3 className="text-md font-bold text-primary mb-4 flex-shrink-0">Chỉnh sửa vai trò: {cleanRoleName(editingRole.tenVaiTro)}</h3>

            {modalError && (
              <div className="p-3 mb-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl flex-shrink-0">
                ⚠️ {modalError}
              </div>
            )}

            <form onSubmit={handleUpdateRole} className="space-y-4 flex-1 flex flex-col min-h-0">
              <div className="grid grid-cols-2 gap-4 flex-shrink-0">
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Tên nhóm vai trò</label>
                  <input
                    required
                    disabled={['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_CASHIER', 'ROLE_BARISTA', 'ROLE_WAITER'].includes(editingRole.tenVaiTro)}
                    value={editRoleName}
                    onChange={(e) => setEditRoleName(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:ring-1 focus:ring-primary/50 outline-none disabled:opacity-40"
                    placeholder="Mã vai trò..."
                    type="text"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">Mô tả vai trò</label>
                  <input
                    value={editRoleDesc}
                    onChange={(e) => setEditRoleDesc(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg px-3 py-2 text-xs text-white focus:ring-1 focus:ring-primary/50 outline-none"
                    placeholder="Mô tả quyền lợi..."
                    type="text"
                  />
                </div>
              </div>

              <div className="space-y-1.5 flex-1 flex flex-col min-h-0">
                <label className="text-[10px] font-bold uppercase tracking-widest text-on-surface-variant flex-shrink-0">Tích chọn các quyền được phép thực hiện</label>
                <div className="flex-1 overflow-y-auto border border-border-glass rounded-xl p-3 bg-[#131929] space-y-4">
                  {groupPermissions(permissions).map((group) => (
                    <div key={group.title} className="p-3 bg-white/5 border border-white/5 rounded-xl space-y-3">
                      <div className="flex items-center gap-2 border-b border-white/10 pb-1.5 mb-1">
                        <span className="material-symbols-outlined text-xs text-primary">{group.icon}</span>
                        <span className="text-[10px] font-bold text-white uppercase tracking-wider">{group.title}</span>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {group.items.map((perm) => {
                          const display = PERMISSION_DISPLAY_NAMES[perm.tenQuyen] || { label: perm.tenQuyen, desc: perm.moTa };
                          return (
                            <label
                              key={perm.id}
                              className="flex items-start gap-2.5 p-2 rounded-lg border border-white/5 bg-white/1 hover:bg-white/5 transition-colors cursor-pointer select-none"
                            >
                              <input
                                type="checkbox"
                                checked={editRolePerms.includes(perm.tenQuyen)}
                                onChange={() => togglePermissionInList(perm.tenQuyen, false)}
                                className="rounded border-white/20 bg-surface-lowest text-primary focus:ring-0 cursor-pointer h-3.5 w-3.5 mt-0.5"
                              />
                              <div className="min-w-0">
                                <p className="text-xs font-bold text-white">{display.label}</p>
                                <p className="text-[9px] text-on-surface-variant mt-0.5 leading-tight">{display.desc}</p>
                              </div>
                            </label>
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex gap-3 pt-3 flex-shrink-0">
                <button
                  type="submit"
                  className="flex-1 bg-primary text-on-primary font-bold py-2.5 rounded-lg text-xs hover:scale-[1.01] active:scale-95 transition-all cursor-pointer"
                >
                  Lưu thay đổi
                </button>
                <button
                  type="button"
                  onClick={() => setIsEditRoleOpen(false)}
                  className="flex-1 bg-white/5 border border-border-glass text-white font-bold py-2.5 rounded-lg text-xs hover:bg-white/10 transition-all cursor-pointer text-center"
                >
                  Hủy bỏ
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
