package com.example.todoapp.logic.motto

object QuoteFormatter {
    fun format(q: QuoteModel): String {
        val author = when {
            //优先取作者
            !q.fromWho.isNullOrBlank() -> q.fromWho
            !q.from.isNullOrBlank() -> q.from
            else -> "佚名"
        }
        val text = q.quote.orEmpty()
        return "「$text」—— $author"
    }
}