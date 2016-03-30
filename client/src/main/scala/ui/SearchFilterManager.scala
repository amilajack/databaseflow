package ui

import java.util.UUID

import org.scalajs.jquery.JQuery

object SearchFilterManager {
  private[this] def savedQueryFields(id: String) = SavedQueryManager.savedQueries.get(UUID.fromString(id)) match {
    case Some(sq) => Seq(id, sq.title, sq.sql)
    case None => Seq(id)
  }
  private[this] def tableFields(id: String) = TableManager.tables.get(id) match {
    case Some(t) => Seq(id, t.description.getOrElse("")) ++ t.columns.map(_.name)
    case None => Seq(id)
  }
  private[this] def viewFields(id: String) = ViewManager.views.get(id) match {
    case Some(v) => Seq(id, v.description.getOrElse("")) ++ v.columns.map(_.name)
    case None => Seq(id)
  }
  private[this] def procedureFields(id: String) = ProcedureManager.procedures.get(id) match {
    case Some(p) => Seq(id, p.description.getOrElse("")) ++ p.params.map(_.name)
    case None => Seq(id)
  }

  def filterSchema(searches: Seq[String]) = {
    MetadataManager.savedQueries.foreach { savedQueries =>
      filterObjects("saved-query", savedQueries, searches, SearchManager.savedQueriesToggle, savedQueryFields)
    }
    MetadataManager.tables.foreach { tables =>
      filterObjects("table", tables, searches, SearchManager.tablesToggle, tableFields)
    }
    MetadataManager.views.foreach { views =>
      filterObjects("view", views, searches, SearchManager.viewsToggle, viewFields)
    }
    MetadataManager.procedures.foreach { procedures =>
      filterObjects("procedure", procedures, searches, SearchManager.proceduresToggle, procedureFields)
    }
  }

  private[this] def highlightMatches(title: String, matches: Seq[String], j: JQuery) = {
    val replaced = matches.foldLeft(title) { (x, y) =>
      val titleLc = title.toLowerCase
      val idx = titleLc.indexOf(y)
      if (idx == -1) { title } else { s"""${title.substring(0, idx)}[[${title.substring(idx, idx + y.length)}]]${title.substring(idx + y.length)}""" }
    }
    val html = replaced.replaceAllLiterally("[[", "<strong class=\"search-matched-text\">").replaceAllLiterally("]]", "</strong>")
    j.html(html)
  }

  private[this] def filterObjects(key: String, seq: Seq[(String, JQuery, JQuery)], searches: Seq[String], toggle: JQuery, searchF: (String) => Seq[String]) = {
    val (matched, notMatched) = seq.partition(t => searchF(t._1).exists(matchName(searches, _)))
    matched.foreach { o =>
      highlightMatches(o._3.attr("title").getOrElse(o._1), searches, o._3)
      o._2.show()
    }
    notMatched.foreach(_._2.hide())
    if (matched.isEmpty) { SearchManager.closeIfOpen(toggle) } else { SearchManager.openIfClosed(toggle) }
    //utils.Logging.info(s"Matched [${matched.size}] and skipped [${notMatched.size}] ${key}s.")
  }

  private[this] def matchName(searches: Seq[String], name: String) = {
    val lcn = name.toLowerCase
    searches.forall(s => lcn.contains(s))
  }
}
