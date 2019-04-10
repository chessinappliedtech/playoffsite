<table class="table table-bordered table-hover">
    <caption style="text-align: left;caption-side: top">
        <h3>${getLevelDescription()}</h3>
    </caption>
    <thead class="thead-light">
    <tr>
        <#list getHeaderRowView().getCells() as headerCell>
            <th class="text-center">${headerCell.getValue()}</th>
        </#list>
    </tr>
    </thead>
    <tbody>
    <#list getPlayerRowViews() as playerRow>
        <tr>
            <#list playerRow.getCells() as cell>
                <#if cell.getStyle()??>
                    <td class="${cell.getStyle()}">
                <#else>
                    <td class="text-center fixed-square">
                </#if>
                    <#if cell.getLink()??>
                        <a href="${cell.getLink()}">${cell.getValue()}</a>
                    <#else>
                        ${cell.getValue()}
                    </#if>
                </td>
            </#list>
        </tr>
    </#list>
    </tbody>
</table>
