#define UNW_LOCAL_ONLY
#include <stdlib.h>
#include <libunwind.h>

int get_callinfo(char *fname, size_t fnlen, unsigned long long *ofs)
{
  unw_context_t context;
  unw_cursor_t cursor;

  unw_getcontext(&context);
  unw_init_local(&cursor, &context);
 
  int i;
  unw_word_t offset, pc;
  for(i=0; i < 3; i++){
      unw_step(&cursor);
      unw_get_reg(&cursor, UNW_REG_IP, &pc);
      if(unw_get_proc_name(&cursor, fname, fnlen, &offset) == 0){
          *ofs = (unsigned long long)offset-5;
      }
  }
  return 0;
}
