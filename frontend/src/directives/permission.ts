import type { Directive, DirectiveBinding } from 'vue'
import { hasRole } from '@/utils/permission'

/**
 * 按钮级权限指令
 * 用法：v-permission="['ADMIN']" 或 v-permission="['ADMIN', 'TEACHER']"
 * 当前角色不在允许列表时直接从 DOM 中移除元素。
 * 表格列不适合用指令移除，请在 el-table-column 上使用 v-if。
 */
export const permission: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    if (!hasRole(binding.value)) {
      el.parentNode?.removeChild(el)
    }
  }
}

export default permission
