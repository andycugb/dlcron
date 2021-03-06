HOW TO USE

说明：
1. 简易分布式任务锁，通过多台机器执行job时，每台机器通过zk的分布式锁来保证唯一机器执行。
注：此分布式锁主要解决执行任务的单点问题，只保证一个任务一定会被至少执行一次，并不保证一个任务只执行一次，因此需要业务方保证任务的幂等性，
    即多次执行同一任务得到相同结果。
2. 整合了quartz调度器，并根据quartz调度器的触发时间的时间戳，保证一次任务执行时分布式锁唯一性，请确保每次触发任务的间隔要大于任务执行所需的时间。
   否则quartz会将下一次任务放入delay队列延后触发，每台机器根据性能不同可能会导致延后触发时间不一致，导致锁不唯一。
   如：一个job执行完需要5秒，请保证每次任务执行触发时间的间隔大于5秒。
3. 在zk中注册锁节点路径：
   a.产品执行root节点,(格式"/${root}/${product}/${jobName}/${执行时间}")
   b.加锁节点，为"产品执行root节点"的子节点(/${root}/${product}/${jobName}/${执行时间}/lock/${各个锁节点})
   例：产品guess运行时，加锁时间级别定义到分钟级："产品执行root节点"为：/${root}/${product}/${jobName}/201601191109，加锁节点为：
   "/${root}/${product}/${jobName}/${执行时间}/lock/_c_9053a307-b551-42c6-a67f-d3c5a111b8e6-lock-0000000000"
   c.任务执行状态在"产品执行root节点"下保存，分为：DOING，DONE