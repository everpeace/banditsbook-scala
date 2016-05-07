#
# The MIT License (MIT)
# 
# Copyright (c) 2016 Shingo Omura
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

library(dplyr)
library(ggplot2)

plot_cumulative_rewards_with_hyper_param <- function(df){
  cumStat <- df %>%
    dplyr::group_by(hyper_param, step) %>%
    dplyr::summarise(cumulative_reward_min    = min(cumulative_reward),
                     cumulative_reward_sd_min = mean(cumulative_reward) - sd(cumulative_reward),
                     cumulative_reward_mean   = mean(cumulative_reward),
                     cumulative_reward_sd_max = mean(cumulative_reward) + sd(cumulative_reward),
                     cumulative_reward_max    = max(cumulative_reward)
                     )


  g <- ggplot(data = cumStat,
              aes(x = step,
                  y = cumulative_reward_mean,
                  colour = hyper_param))
  g <- g + geom_point(size=0.5)
  g <- g + geom_line(size=0.5)
  g <- g + geom_ribbon(alpha = 0.1, colour=NA,
                       aes(
                         fill = hyper_param,
                         ymin=cumulative_reward_min,
                         ymax = cumulative_reward_max
                      ))
  g <- g + geom_ribbon(alpha = 0.2, colour=NA,
                       aes(
                         fill = hyper_param,
                         ymin = cumulative_reward_sd_min,
                         ymax = cumulative_reward_sd_max
                       ))
  g <- g + facet_grid(. ~ hyper_param)
  g <- g + theme(legend.position="none")
  g
}

plot_cumulative_rewards_without_hyper_param <- function(df){
  cumStat <- df %>%
    dplyr::group_by(step) %>%
    dplyr::summarise(cumulative_reward_min = min(cumulative_reward),
                     cumulative_reward_sd_min = mean(cumulative_reward) - sd(cumulative_reward),
                     cumulative_reward_mean   = mean(cumulative_reward),
                     cumulative_reward_sd_max = mean(cumulative_reward) + sd(cumulative_reward),
                     cumulative_reward_max = max(cumulative_reward))
  g <- ggplot(data = cumStat,
              aes(x = step,
                  y = cumulative_reward_mean))
  g <- g + geom_point(size=0.5)
  g <- g + geom_line(size=0.5)
  g <- g + geom_ribbon(alpha = 0.1, colour=NA,
                       aes(
                         ymin=cumulative_reward_min,
                         ymax = cumulative_reward_max
                      ))
  g <- g + geom_ribbon(alpha = 0.2, colour=NA,
                       aes(
                         ymin = cumulative_reward_sd_min,
                         ymax = cumulative_reward_sd_max
                       ))
  g <- g + theme(legend.position="none")
  g
}
