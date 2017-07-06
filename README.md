# TCO-2017-Marathon-Round-3

## link

https://community.topcoder.com/longcontest/?module=ViewProblemStatement&rd=16944&pm=14636
https://community.topcoder.com/longcontest/?module=ViewStandings&rd=16944
https://community.topcoder.com/longcontest/?module=ViewActiveContests&rt=13

## 感想

うーん、どうなんだろうっていう問題
とりあえず探索する問題じゃない

フィルターを作って論理和で返ってくる
毒を見逃すとスコア0なので、安全にふる必要がある

期待値を算出する問題？

testが少ない場合
例えば、testが1回しかできなくて、test回数以上の毒がある時は
毒がどこにあるか辺りをつけるのは不可能で、和が0になるようにフィルターを小さくして、フィルター内に毒が無いことを確認するしかない

bottles = 100
strips = 1
rounds = 1
poison = 2

だとすると、1 poison / 50 bottlesだけど
filter = 50にすると
hit: 0.75 -> worst
hit: 0.25 -> high
相対的に高いスコアを得られるかどうかだから、実質的に正解はない
